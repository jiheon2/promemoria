// API 서버 정보 기입
// const apiServer = "gateway:11000";
const loginPage = "/user/login.html"
let loginUserId = "";

// 로그인 정보가 있는지 확인
function getToken() {
    // 쿠키에서 Access Token을 읽어오는 함수
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }

    // 쿠키에서 JWT AccessToken 가져오기
    const jwtAccessToken = getCookie("accessToken"); // 쿠키 이름이 'accessToken'이라고 가정

    $.ajax({
        url: "http://localhost:13000/user/v1/getToken",
        type: "post",
        dataType: "json",
        headers: {
            "Authorization": `Bearer ${jwtAccessToken}`
        },
        xhrFields: {
            withCredentials: true
        },
        success: function (json) {
            console.log(json); // 서버에서 받은 토큰 정보 출력
            loginUserId = json.userId;
            console.log(loginUserId);
            userInfo();
        },
        error: function () {
            alert("로그인 해주시길 바랍니다.");
            location.href = loginPage;
            console.log(error);
        }
    });
}