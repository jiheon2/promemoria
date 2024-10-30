// peer 생성 + 정보 교환 + offer/answer 처리

// 1. 웹 캠 여는 함수
let localStreamElement = document.querySelector('#localVideoCallStream');
// 식별하기 위한 random key
const myKey = Math.random().toString(36).substring(2, 11);
let pcListMap = new Map();
let roomId;
let otherKeyList = [];
let localStream = undefined;

// 1. 웹 캠 여는 함수
const startCam = async () => {
    if (navigator.mediaDevices !== undefined) {
        await navigator.mediaDevices.getUserMedia({audio: true, video: true})
            .then(async (stream) => {
                console.log('Stream found');
                //웹캠, 마이크의 스트림 정보를 글로벌 변수로 저장한다.
                localStream = stream;
                // Disable the microphone by default
                stream.getAudioTracks()[0].enabled = true;
                localStreamElement.srcObject = localStream;
                // Connect after making sure that local stream is availble

            }).catch(error => {
                console.error("Error accessing media devices:", error);
            });
    }

}


let screenStream; // 전역 변수로 정의하여 startRecording과 stopRecording에서 접근 가능
let recordedChunks = [];
let mediaRecorder;
let recordedBlob;
let intervalId;
let splitChunks = [];   // 1분 단위로 분할된 파일을 저장할 배열
let partCounter = 1;  // 분할 파일 번호 카운터

// 화면 캡처 및 스트림을 표시할 비디오 요소를 참조
const videoElem = document.getElementById('localVideoCallStream');

const downloadButton = document.getElementById('uploadRecordedFile');

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

            document.getElementById('startScreenRecordBtn').style.display = 'none';
            document.getElementById('stopScreenRecordBtn').style.display = 'block';
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

    document.getElementById('stopScreenRecordBtn').style.display = 'none';
}


// 2. 웹 소켓 연결 함수
const connectSocket = async () => {
    const socket = new SockJS('/signaling');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({'X-User-Id': 'bread'}, function () {
        console.log('Connected to WebRTC server');

        // 연결이 완료된 후 자신의 키를 다른 참가자들에게 전송
        stompClient.send(`/studyRoom/call/key`, {}, {});

        setTimeout(() => {
            otherKeyList.map((key) => {
                if (!pcListMap.has(key)) {
                    pcListMap.set(key, createPeerConnection(key));
                    sendOffer(pcListMap.get(key), key); // 각 참가자에게 연결 요청
                }
            });
        }, 1000);

        // 필요한 구독 설정들 (iceCandidate, offer, answer 등)
        stompClient.subscribe(`/videoCall/peer/iceCandidate/${myKey}/${roomId}`, candidate => {
            const key = JSON.parse(candidate.body).key;
            const message = JSON.parse(candidate.body).body;

            pcListMap.get(key).addIceCandidate(new RTCIceCandidate({
                candidate: message.candidate,
                sdpMLineIndex: message.sdpMLineIndex,
                sdpMid: message.sdpMid
            }));
        });

        stompClient.subscribe(`/videoCall/peer/offer/${myKey}/${roomId}`, offer => {
            const key = JSON.parse(offer.body).key;
            const message = JSON.parse(offer.body).body;

            pcListMap.set(key, createPeerConnection(key));
            pcListMap.get(key).setRemoteDescription(new RTCSessionDescription({
                type: message.type,
                sdp: message.sdp
            }));
            sendAnswer(pcListMap.get(key), key);
        });

        stompClient.subscribe(`/videoCall/peer/answer/${myKey}/${roomId}`, answer => {
            const key = JSON.parse(answer.body).key;
            const message = JSON.parse(answer.body).body;

            pcListMap.get(key).setRemoteDescription(new RTCSessionDescription(message));
        });

        stompClient.subscribe(`/videoCall/call/key`, message => {
            stompClient.send(`/videoCall/send/key`, {}, JSON.stringify(myKey));
        });

        stompClient.subscribe(`/videoCall/send/key`, message => {
            const key = JSON.parse(message.body);
            if (myKey !== key && !otherKeyList.includes(key)) {
                otherKeyList.push(key);
            }
        });

        console.log(`${roomId} 채팅방에 입장했습니다.`);
    });
};

// const connectSocket = async () => {
//     const socket = new SockJS('/signaling');
//     stompClient = Stomp.over(socket);
//     stompClient.debug = null;
//
//     stompClient.connect({'X-User-Id': 'bread'}, // 테스트용 헤더
//         function () {
//             console.log('Connected to WebRTC server');
//
//             // 이미 만들어진 방에 입장하기 위한 구독 설정들
//             stompClient.subscribe(`/videoCall/peer/iceCandidate/${myKey}/${roomId}`, candidate => {
//                 const key = JSON.parse(candidate.body).key;
//                 const message = JSON.parse(candidate.body).body;
//
//                 pcListMap.get(key).addIceCandidate(new RTCIceCandidate({
//                     candidate: message.candidate,
//                     sdpMLineIndex: message.sdpMLineIndex,
//                     sdpMid: message.sdpMid
//                 }));
//             });
//
//             stompClient.subscribe(`/videoCall/peer/offer/${myKey}/${roomId}`, offer => {
//                 const key = JSON.parse(offer.body).key;
//                 const message = JSON.parse(offer.body).body;
//
//                 pcListMap.set(key, createPeerConnection(key));
//                 pcListMap.get(key).setRemoteDescription(new RTCSessionDescription({
//                     type: message.type,
//                     sdp: message.sdp
//                 }));
//                 sendAnswer(pcListMap.get(key), key);
//             });
//
//             stompClient.subscribe(`/videoCall/peer/answer/${myKey}/${roomId}`, answer => {
//                 const key = JSON.parse(answer.body).key;
//                 const message = JSON.parse(answer.body).body;
//
//                 pcListMap.get(key).setRemoteDescription(new RTCSessionDescription(message));
//             });
//
//             stompClient.subscribe(`/videoCall/call/key`, message => {
//                 stompClient.send(`/videoCall/send/key`, {}, JSON.stringify(myKey));
//             });
//
//             stompClient.subscribe(`/videoCall/send/key`, message => {
//                 const key = JSON.parse(message.body);
//                 if (myKey !== key && !otherKeyList.includes(key)) {
//                     otherKeyList.push(key);
//                 }
//             });
//
//             console.log(`${roomId} 채팅방에 입장했습니다.`);
//
//
//         }
//
//     );
// };


// 3. peerConnection 생성해주는 함수
const createPeerConnection = (otherKey) => {
    const pc = new RTCPeerConnection();
    try {
        // peerConnection에서 icecandidate 이벤트가 발생 시 onIceCandidate 함수 실행
        pc.addEventListener('icecandidate', (event) => {
            onIceCandidate(event, otherKey);
        });
        // peerConnection에서 track 이벤트가 발생 시 onTrack 함수 실행
        pc.addEventListener('track', (event) => {
            onTrack(event, otherKey);
        });
        // 만약 localStream이 존재하면 peerConnection에 addTrack 으로 추가
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

// onIceCandidate 함수 추가
let onIceCandidate = (event, otherKey) => {
    if (event.candidate) {
        console.log('ICE candidate');
        stompClient.send(`/studyRoom/peer/iceCandidate/${otherKey}/${roomId}`, {}, JSON.stringify({
            key: myKey,
            body: event.candidate
        }));
    }
};

//onTrack 함수 추가
let onTrack = (event, otherKey) => {
    if (document.getElementById(`${otherKey}`) === null) {
        const video = document.createElement('video');

        video.autoplay = true;
        video.controls = true;
        video.id = otherKey;
        video.srcObject = event.streams[0];

        document.getElementById('remoteVideoCallStream').appendChild(video);
    }
};

// offer 함수
let sendOffer = (pc, otherKey) => {
    pc.createOffer().then(offer => {
        setLocalAndSendMessage(pc, offer);
        stompClient.send(`/studyRoom/peer/offer/${otherKey}/${roomId}`, {}, JSON.stringify({
            key: myKey,
            body: offer
        }));
        console.log('Send offer');
    });
};

// answer 함수
let sendAnswer = (pc, otherKey) => {
    pc.createAnswer().then(answer => {
        setLocalAndSendMessage(pc, answer);
        stompClient.send(`/studyRoom/peer/answer/${otherKey}/${roomId}`, {}, JSON.stringify({
            key: myKey,
            body: answer
        }));
        console.log('Send answer');
    });
};

const setLocalAndSendMessage = (pc, sessionDescription) => {
    pc.setLocalDescription(sessionDescription);
}

function displayNotification(notification) {
    const notificationElement = document.createElement('p');
    notificationElement.innerText = notification;
    document.getElementById('notifications').appendChild(notificationElement);
}


<!-- 화상 채팅 버튼 이벤트들 -->
// html 버튼 이벤트(캠 + 웹 소켓 실행)
document.querySelector('#enterRoomBtn').addEventListener('click', async () => {
    // 화면 바꾸는 로직이 들어가야됨!!!
    await startCam();  // 웹 캠 실행

    // startCam()이 실행 됐으면 현재 localStream은 stream 상태임(아래 조건문이 true 라는 것)
    if (localStream !== undefined) {
        document.querySelector('#remoteVideoCallStream').style.display = 'block';  // display: block은 해당 요소를 화면에 보이게 하고, 블록 요소로 취급하게 만듦
        document.querySelector('#startStreamBtn').style.display = '';              // 시작하기 버튼 숨김 처리 해제
    }
    // roomId = document.querySelector('#roomId').value;
    // document.querySelector('#roomId').disabled = true;       // disabled = true는 HTML 요소가 비활성화되도록 설정하는 것
    document.querySelector('#enterRoomBtn').disabled = true; // -> input에는 값을 입력할 수 없고, 버튼은 클릭할 수 없음

    await connectSocket();  // 웹 소켓 연결 + 방 생성
});

// 스트림 버튼 클릭시 , 다른 웹 key들 웹소켓을 가져 온뒤에 offer -> answer -> iceCandidate 통신
// peer 커넥션은 pcListMap 으로 저장
// 유저 A가 스트림을 시작할 때 otherKeyList에 있는 다른 유저들(여기서는 유저 B)에게 연결 요청을 보낸다.
// setTimeout 내부의 otherKeyList.map 부분에서 각 key에 대해 createPeerConnection을 호출하고, 해당 피어에게 sendOffer 메서드를 통해 offer를 보낸다.
// 이때 유저 B는 connectSocket 함수의 offer 관련 subscribe를 통해 이 offer를 수신하고, 이를 통해 연결이 시작된다.
// new! startStreamBtn 클릭하면 createConnection 함수 호출 되면서 '동일 채팅방 내 다른 사용자'들과 연결됨
document.querySelector('#startStreamBtn').addEventListener('click', async () => {
    await stompClient.send(`/studyRoom/call/key`, {}, {});

    setTimeout(() => {

        otherKeyList.map((key) => {
            if (!pcListMap.has(key)) {
                pcListMap.set(key, createPeerConnection(key));
                sendOffer(pcListMap.get(key), key);
            }

        });

    }, 1000);
});


<!-- 녹화/다운 버튼 이벤트들 -->
// 다운로드 버튼 클릭 이벤트: 분할된 파일을 사용자가 직접 다운로드할 수 있게 함
// document.getElementById('uploadRecordedFile').addEventListener('click', () => {
//     if (splitChunks.length > 0) {
//         splitChunks.forEach((blob, index) => {
//             const partURL = URL.createObjectURL(blob);
//             const a = document.createElement('a');
//             a.style.display = 'none';
//             a.href = partURL;
//             a.download = `RecordedVideo_part${index + 1}.mp4`;
//             document.body.appendChild(a);
//             a.click();
//             document.body.removeChild(a);
//             console.log(`Downloaded part ${index + 1}`);
//         });
//     } else {
//         console.error('No split video parts available for download');
//     }
//
//     // 전체 녹화 파일 다운로드
//     if (recordedBlob) {
//         const recordingURL = URL.createObjectURL(recordedBlob);
//         const a = document.createElement('a');
//         a.style.display = 'none';
//         a.href = recordingURL;
//         a.download = 'RecordedVideo.mp4';
//         document.body.appendChild(a);
//         a.click();
//         document.body.removeChild(a);
//         console.log('Download initiated');
//     }
// });

// 버튼 클릭 이벤트 연결
document.getElementById('startScreenRecordBtn').addEventListener('click', async () => {
    await startRecording();
});

document.getElementById('stopScreenRecordBtn').addEventListener('click', async () => {
    await stopRecording();
});


document.getElementById('uploadRecordedFile').addEventListener('click', () => {
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

    // 서버로 formData 전송
    fetch('file/uploadVideoChat', {
        method: 'POST',
        body: formData,
    })
        .then(response => response.json())
        .then(data => {
            console.log('Upload successful:', data);
        })
        .catch(error => {
            console.error('Upload failed:', error);
        });
});
