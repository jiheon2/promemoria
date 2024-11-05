// let remoteStreamElement = document.querySelector('#remoteStream');
let localStreamElement = document.querySelector('#localStream');
const myKey = Math.random().toString(36).substring(2, 11);
let pcListMap = new Map();
let roomName;
let userName;
let otherKeyList = [];
let localStream = undefined;

roomName = localStorage.getItem("roomName")
userName = localStorage.getItem("userName");

$.ajax({
    url: "/video/createRoom",
    type: "get",
    data: {
        roomName : roomName,
        userName : userName
    },
    success: function (json) {
        
        localStorage.removeItem("roomName")

        // 웹소켓 객체를 생성하는 중
        if (ws !== undefined && ws.readyState !== WebSocket.CLOSED) {
            console.log("WebSocket is already opened.");
            return;
        }

        // 접속 URL 예 : ws://localhost:10000/ws/테스트방/별명
        ws = new WebSocket("wss://" + location.host + "/ws/" + roomName + "/" + userName);

        // 웹소켓 열기
        ws.onopen = function (event) {
            if (event.data === undefined)
                return;

            console.log(event.data)
        };

        // 메시지 수신 시 처리
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);

            if (data.state === "참여자목록") {
                // 기존 참여자 목록을 초기 설정
                if (data.msg !== undefined) participantList.innerText = data.msg;

            } else if (data.state === "입장") {
                // 입장한 유저 추가
                addParticipant(data.msg);
                // 방 개설 시간 설정 (첫 번째 유저 입장 시에만)
                if (!isRoomOpened) {
                    openingDate.textContent = getFormattedDate(new Date());
                    isRoomOpened = true;
                }

            } else if (data.state === "퇴장") {
                // 퇴장한 유저 제거
                removeParticipant(data.msg);
            }
        };
    }
})

const startCam = async () => {

    // 비디오 요소를 찾을 때까지 기다리도록 함수를 변경합니다.
    const localStreamElement = document.querySelector('#localVideoCallStream');

    if (localStreamElement) {

        if (navigator.mediaDevices !== undefined) {
            await navigator.mediaDevices.getUserMedia({audio: true, video: true})
                .then(async (stream) => {

                    localStream = stream;

                    // Enable the cam by default
                    stream.getVideoTracks()[0].enabled = true;

                    // Disable the microphone by default
                    stream.getAudioTracks()[0].enabled = false;

                    // 비디오 요소를 표시하기 전에 display 속성을 변경
                    document.querySelector('#localVideoCallStream').style.display = 'block';

                    localStreamElement.srcObject = localStream;
                    console.log("localStreamElement.srcObject : ", localStreamElement.srcObject)

                    // Connect after making sure that local stream is availble

                }).catch(error => {
                    console.error("Error accessing media devices:", error);
                });

        } else {

            console.error("Local video element not found");

        }

    }
}

const connectSocket = async () => {
    const socket = new SockJS('/signaling');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({
        'roomName': roomName,
        'camKey': myKey
    }, function () {
        console.log('Connected to WebRTC server');

        // iceCandidate 를 구독 해준다.
        stompClient.subscribe(`/topic/peer/iceCandidate/${myKey}/${roomName}`, candidate => {
            const key = JSON.parse(candidate.body).key
            const message = JSON.parse(candidate.body).body;

            //해당 신호를 Peer에 추가해준다.
            pcListMap.get(key).addIceCandidate(new RTCIceCandidate({
                candidate: message.candidate,
                sdpMLineIndex: message.sdpMLineIndex,
                sdpMid: message.sdpMid
            }));

        });

        //offer 를 구독 해준다.
        stompClient.subscribe(`/topic/peer/offer/${myKey}/${roomName}`, offer => {

            const key = JSON.parse(offer.body).key;
            const message = JSON.parse(offer.body).body;

            //해당 키에 대한 새로운 peer를 생성하여 map 에 저장한다.
            pcListMap.set(key, createPeerConnection(key));
            //새로 만든 peer에 RTCSessionDescription를 추가해준다.
            pcListMap.get(key).setRemoteDescription(new RTCSessionDescription({type: message.type, sdp: message.sdp}));
            //받은 키에 대한 answer를 보낸다.
            sendAnswer(pcListMap.get(key), key);

        });

        //answer 를 구독 해준다.
        stompClient.subscribe(`/topic/peer/answer/${myKey}/${roomName}`, answer => {
            const key = JSON.parse(answer.body).key;
            const message = JSON.parse(answer.body).body;

            //받은 키에 대한 peer에 description 해준다.
            pcListMap.get(key).setRemoteDescription(new RTCSessionDescription(message));

        });

        stompClient.subscribe(`/topic/call/key`, message => {
            stompClient.send(`/app/send/key`, {}, JSON.stringify(myKey));

        });

        stompClient.subscribe(`/topic/send/key`, message => {
            const key = JSON.parse(message.body);

            if (myKey !== key && otherKeyList.find((mapKey) => mapKey === myKey) === undefined) {
                otherKeyList.push(key);
            }
        });

        // 연결 종료 알림을 처리하는 구독 추가
        stompClient.subscribe(`/topic/disconnect/${roomName}`, message => {
            const key = JSON.parse(message.body).key;
            console.log(`Peer disconnected: ${key}`);

            // 원격 비디오 요소 제거
            const remoteVideoElement = document.getElementById(key);

            if (remoteVideoElement) {
                remoteVideoElement.srcObject = null;
                remoteVideoElement.remove();
            }
        });

        // 웹소켓 연결이 끊어졌을 때 처리
        socket.onclose = () => {
            console.log('Disconnected from WebRTC server');
            // 로컬 스트림 비디오 요소 제거
            if (localStreamElement) {
                localStreamElement.srcObject = null;
                localStreamElement.style.display = 'none';
            }

            // 다른 사용자들에게 연결 종료 알림
            stompClient.send(`/app/disconnect/${roomName}`, {}, JSON.stringify({key: myKey}));
        };

        // 사용자가 페이지를 떠날 때 서버로 연결 종료 알림을 보냄
        window.onbeforeunload = () => {

            // 원격 비디오 요소 제거
            // const remoteVideoElement = document.getElementById(key);
            const remoteVideoElement = document.getElementById(myKey);

            if (remoteVideoElement) {
                remoteVideoElement.srcObject = null;
                remoteVideoElement.remove();
            }

            // 로컬 스트림 비디오 요소 제거
            if (localStreamElement) {
                localStreamElement.srcObject = null;
                localStreamElement.style.display = 'none';
            }

            // 서버로 연결 종료 알림 전송
            stompClient.send(`/app/disconnect/${roomName}`, {}, JSON.stringify({key: myKey}));
        };

        // 웹소켓 연결 완료 후 startStream 함수 호출
        startStream();

    });
}

let onTrack = (event, otherKey) => {

    if (document.getElementById(`${otherKey}`) === null) {
        const video = document.createElement('video');

        video.autoplay = true;
        video.controls = true;
        video.id = otherKey;
        video.srcObject = event.streams[0];

        document.getElementById('remoteStreamDiv').appendChild(video);
    }

    //
    // remoteStreamElement.srcObject = event.streams[0];
    // remoteStreamElement.play();
};

const createPeerConnection = (otherKey) => {
    const pc = new RTCPeerConnection();
    try {
        pc.addEventListener('icecandidate', (event) => {
            onIceCandidate(event, otherKey);
        });
        pc.addEventListener('track', (event) => {
            onTrack(event, otherKey);
        });
        if (localStream !== undefined) {
            localStream.getTracks().forEach(track => {
                pc.addTrack(track, localStream);
            });
        }

        console.log('PeerConnection created');
    } catch (error) {
        console.error('PeerConnection failed: ', error);
    }
    return pc;
}

let onIceCandidate = (event, otherKey) => {
    if (event.candidate) {
        console.log('ICE candidate');
        stompClient.send(`/app/peer/iceCandidate/${otherKey}/${roomName}`, {}, JSON.stringify({
            key: myKey,
            body: event.candidate
        }));
    }
};

let sendOffer = (pc, otherKey) => {
    pc.createOffer().then(offer => {
        setLocalAndSendMessage(pc, offer);
        stompClient.send(`/app/peer/offer/${otherKey}/${roomName}`, {}, JSON.stringify({
            key: myKey,
            body: offer
        }));
        console.log('Send offer');
    });
};

let sendAnswer = (pc, otherKey) => {
    pc.createAnswer().then(answer => {
        setLocalAndSendMessage(pc, answer);
        stompClient.send(`/app/peer/answer/${otherKey}/${roomName}`, {}, JSON.stringify({
            key: myKey,
            body: answer
        }));
        console.log('Send answer');
    });
};

const setLocalAndSendMessage = (pc, sessionDescription) => {
    pc.setLocalDescription(sessionDescription);
}

//룸 번호 입력 후 캠 + 웹소켓 실행
// document.querySelector('#enterRoomBtn').addEventListener('click', async () =>{

$(document).ready(async function () {
    // 캠 시작
    await startCam();

    // 웹소켓 연결
    await connectSocket();
})

// 스트림 버튼 클릭시 , 다른 웹 key들 웹소켓을 가져 온뒤에 offer -> answer -> iceCandidate 통신
// peer 커넥션은 pcListMap 으로 저장
const startStream = async () => {

    await stompClient.send(`/app/call/key`, {}, {});

    setTimeout(() => {

        otherKeyList.map((key) => {
            if (!pcListMap.has(key)) {
                pcListMap.set(key, createPeerConnection(key));
                sendOffer(pcListMap.get(key), key);
            }

        });

    }, 1000);

};

let screenStream; // 전역 변수로 정의하여 startRecording과 stopRecording에서 접근 가능
let recordedChunks = [];
let mediaRecorder;
let recordedBlob;
let intervalId;
let splitChunks = [];   // 1분 단위로 분할된 파일을 저장할 배열
let partCounter = 1;  // 분할 파일 번호 카운터
let recordYn = "N"; // 초기 녹화 상태

// 화면 캡처 및 스트림을 표시할 비디오 요소를 참조
const videoElem = document.getElementById('localVideoCallStream');

const downloadButton = document.getElementById('videoChatEndBtn');

// 화면 캡처 시 사용할 옵션 설정
const displayMediaOptions = {
    video: {
        cursor: "always", // 화면 공유 중 커서를 항상 보이게 설정
    },
    audio: true // 오디오 캡처를 활성화하려면 true로 설정
};

// MediaRecorder로 녹화할 수 있도록 스트림 데이터를 통합
// 두 가지 다른 스트림을 하나의 비디오 엘리먼트에 적용하려면 스트림을 병합하거나 각각의 스트림을 다른 엘리먼트로 다뤄야 함(두 가지 스트림 -> 웹 캠 실행 + 화면 녹화)
let combinedStream;

// * 화면 녹화 시작 함수(녹화 시작 버튼 눌러서 실행)
// await이 없으면 각 함수의 실행이 다음 단계로 넘어가는 데 방해를 받지 않는다.(await을 많이 쓰면 직렬 처리가 되어 버린다.)
// 즉, startCapture가 실행되는 동안 connectSocket이나 다른 비동기 작업들도 동시에 실행된다.
async function startRecording() {
    try {
        // (1). navigator.mediaDevices.getDisplayMedia()를 호출하면, 브라우저가 자동으로 화면, 특정 애플리케이션 창, 또는 브라우저 탭 중에서 선택할 수 있는 대화 상자를 띄운다.
        // (2). 사용자는 그 대화 상자에서 원하는 공유 대상을 선택한다.
        // (3). 선택한 대상의 스트림이 screenStream에 할당된다.(그 영역이 녹화됨) --> 꼭 선택해야만 하나 자동으론 안 되나요..
        screenStream = await navigator.mediaDevices.getDisplayMedia(displayMediaOptions);
        console.log('Screen recording started');

        if (!localStream) {
            localStream = await navigator.mediaDevices.getUserMedia({video: true, audio: true});
        }

        // 화면, 웹 캠 두 스트림 병합(combinedStream = localStream + screenStream)
        if (localStream && screenStream) {
            combinedStream = new MediaStream([
                ...localStream.getTracks(),
                ...screenStream.getTracks()
            ]);

            // 비디오 요소에 병합된 스트림을 할당
            videoElem.srcObject = combinedStream;

            // MediaRecorder 생성 및 이벤트 처리(MediaRecorder를 combinedStream으로 초기화 -> mediaRecorder는 combineStream을 데이터로 받아 녹화)
            mediaRecorder = new MediaRecorder(combinedStream, {
                mimeType: 'video/webm'
            });

            // MediaRecorder의 ondataavailable 이벤트로 녹화된 데이터를 recordedChunks 배열에 저장
            // ondataavailable 이벤트가 발생할 때마다 recordedChunks에 데이터가 수집되고, 그 데이터를 splitChunks에 Blob 형태로 저장
            mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    recordedChunks.push(event.data);
                    console.log(`Data chunk received, size: ${event.data.size}`);

                    // 분할 데이터를 저장할 조건
                    if (mediaRecorder.state === 'recording' && recordedChunks.length > 0) {
                        // 분할 데이터 저장(각 블롭은 mediaRecorder.requestData()로 요청된 데이터로 생성되며, 이후 별도의 다운로드 링크로 저장됨)
                        const splitBlob = new Blob(recordedChunks, {type: "video/mp4"});
                        // window.open(recordingURL); 새 탭으로 녹화된 영상 확인 -> 없어도 되는 거라 뺌
                        splitChunks.push(splitBlob);  // 배열에 1분 녹화 영상 추가(splitChunks 배열에 1분 단위로 나누어진 Blob 객체들을 저장)
                        console.log(`Part ${partCounter} saved, size: ${splitBlob.size}`);
                        partCounter += 1;   // 1분 녹화 영상 추가되면 카운트
                        recordedChunks = []; // 데이터를 저장한 후, 새로운 데이터 수집을 위해 recordedChunks 초기화
                    }
                }
            };

            // onstop 이벤트에서 녹화된 블롭(recordedBlob)을 생성하고, 이를 다운로드할 수 있는 URL을 만든다.
            mediaRecorder.onstop = () => {
                // * 최종 녹화 데이터 생성(녹화 데이터가 있는지 확인 - 데이터 유효성 검사)
                if (recordedChunks.length > 0) {
                    // recordedBlob이 제대로 생성되지 않으면, 다운로드 링크가 작동하지 않는다.(recordedBlob과 recordingURL을 제대로 생성했는지 확인)
                    recordedBlob = new Blob(recordedChunks, {type: "video/mp4"});
                    const recordingURL = URL.createObjectURL(recordedBlob);  // URL.createObjectURL(recordedBlob)을 통해 URL 생성(Blob URL 생성)

                    // 전체 녹화 파일 다운로드 설정(다운로드 버튼)
                    downloadButton.href = recordingURL;  // 다운로드 버튼의 href 속성에 URL이 설정되지 않으면, 다운로드가 작동하지 않는다.
                    downloadButton.download = "RecordedVideo" + ".mp4";  // 유저 ID랑 날짜 들어가게(파일명 설정)
                    downloadButton.style.display = 'block';
                    console.log('Final recording available for download');
                } else {
                    console.error('No recorded data available');
                }
            };

            // 녹화 시작
            mediaRecorder.start();
            console.log('Recording started');

            // 1분마다 데이터를 요청해 분할 파일을 생성하는 타이머 설정 (1분 = 60,000 ms)
            intervalId = setInterval(() => {
                if (mediaRecorder.state === "recording") {
                    console.log('Requesting data for split...');
                    mediaRecorder.requestData(); // 현재까지의 데이터를 요청(일정 시간마다 데이터 수집 -> 1분씩 잘라서 저장하려고)
                }
            }, 60000); // 1분 간격으로 분할

            document.getElementById('startScreenRecordBtn').disabled = true;
            // document.getElementById('stopScreenRecordBtn').style.display = 'block';
        } else {
            console.error('localStream or screenStream is not available.');
        }
    } catch (err) {
        console.error("Error starting screen capture:", err);
    }
}

// * 녹화 중지 함수
// startRecording 함수에서 시작된 화면 캡처와 웹캠 스트림을 멈추려면, 각 스트림의 트랙들을 중지해야 한다.
// JavaScript에서 스트림을 멈추는 방법은 MediaStreamTrack 객체의 stop() 메서드를 사용하는 것
// 여기서(중지 함수) screenStream과 combinedStream의 모든 트랙을 중지하는 함수를 추가하면 된다.
async function stopRecording() {
    if (mediaRecorder && mediaRecorder.state !== "inactive") {
        mediaRecorder.stop();
        console.log('Recording stopped');
    }

    // 화면 공유 스트림에서 모든 트랙을 가져와 stop()을 호출하여 중지(화면 스트림 중지)
    if (screenStream) {
        screenStream.getTracks().forEach(track => track.stop());
        console.log('Screen capture stopped');
    }

    // 스트림을 중지한 후 videoElem.srcObject를 null로 설정해 비디오 요소에서 스트림 연결을 해제(요소 초기화 == 비디오 요소의 스트림을 제거)
    if (videoElem) {
        videoElem.srcObject = null;
    }

    // document.getElementById('stopScreenRecordBtn').style.display = 'none';
}

// 버튼 클릭 이벤트 연결
document.getElementById('startScreenRecordBtn').addEventListener('click', async () => {
    // 화면 공유 안내 메시지 표시
    const userConfirmed = confirm("화면 녹화를 시작하려면 다음 안내를 따르세요:\n\n1. 크롬 화면 공유 창에서 '창' 탭을 선택하세요.\n2. 현재 보고 있는 창을 선택하여 공유를 시작하세요.\n\n동의하신다면 확인을 눌러주세요.");

    // 사용자가 확인 버튼을 눌렀을 경우에만 녹화 시작
    if (userConfirmed) {
        await startRecording();
        recordYn = "Y";
    }

    // 배경색과 안내 멘트 업데이트
    const recordAlert = document.getElementById('recordAlert');
    recordAlert.classList.replace('bg-danger', 'bg-success');
    recordAlert.textContent = '[화상 채팅 종료]를 누르면 자동으로 녹화가 종료됩니다.';
});

// document.getElementById('stopScreenRecordBtn').addEventListener('click', async () => {
//     await stopRecording();
// });

document.getElementById('videoChatEndBtn').addEventListener('click', async () => {
    if (recordYn === "Y") {
        await stopRecording(); // 녹화 중단을 기다림

        await new Promise(resolve => setTimeout(resolve, 500));

        const formData = new FormData();

        if (splitChunks.length > 0) {
            splitChunks.forEach((blob, index) => {
                formData.append(`videoPart`, blob, `RecordedVideo_part${index + 1}.mp4`);
            });
            console.log("Added split chunks to formData");
        } else {
            console.error('No split video parts available for upload');
        }

        if (recordedBlob) {
            formData.append('fullRecording', recordedBlob, 'RecordedVideo.mp4');
            console.log('Added full recording to formData');
        }

        // userId 추가
        formData.append('userId', loginUserId);

        try {
            const response = await fetch('/file/uploadVideoChat', {
                method: 'POST',
                body: formData,
            });

            // JSON 파싱 전에 응답이 비어있는지 확인
            if (response.ok) {
                const data = await response.json();
                console.log('Upload successful:', data);
                alert('녹화 종료 후 화상 채팅이 종료되었습니다.');
            } else {
                throw new Error('Upload failed');
            }

            location.href = "/video/video-chat.html";
        } catch (error) {
            console.error('Upload failed:', error);
            alert('업로드에 실패했습니다. 다시 시도해주세요.');
        }
    } else {
        alert('화상 채팅이 종료되었습니다.');
        location.href = "/video/video-chat.html";
    }

    // 배경색과 안내 멘트를 원래대로 되돌림
    const recordAlert = document.getElementById('recordAlert');
    recordAlert.classList.replace('bg-success', 'bg-danger');
    recordAlert.textContent = '치매 조기 진단을 위해 [화면 녹화 시작]을 눌러주세요.';
});


// document.getElementById('uploadRecordedFile').addEventListener('click', () => {
//     const formData = new FormData();
//
//     if (splitChunks.length > 0) {
//         splitChunks.forEach((blob, index) => {
//             formData.append(`videoPart`, blob, `RecordedVideo_part${index + 1}.mp4`);
//         });
//         console.log("Added split chunks to formData");
//     } else {
//         console.error('No split video parts available for upload');
//     }
//
//     if (recordedBlob) {
//         formData.append('fullRecording', recordedBlob, 'RecordedVideo.mp4');
//         console.log('Added full recording to formData');
//     }
//
//     // 서버로 formData 전송
//     fetch('/file/uploadVideoChat', {
//         method: 'POST',
//         body: formData,
//     })
//         .then(response => response.json())
//         .then(data => {
//             console.log('Upload successful:', data);
//         })
//         .catch(error => {
//             console.error('Upload failed:', error);
//         });
// });
