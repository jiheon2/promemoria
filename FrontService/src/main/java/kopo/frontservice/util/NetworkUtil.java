package kopo.frontservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Slf4j
public class NetworkUtil {
    public static String get(String apiUrl) {
        return get(apiUrl,null);
    }

    public static String get(String apiUrl, @Nullable Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);

        try {
            con.setRequestMethod("GET");

            /* 전송할 헤더 값이 존재하면 헤더 값 추가하기 */
            if (requestHeaders != null) {
                for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            /* API 호출 후 결과 받기 */
            int responseCode = con.getResponseCode();

            /* API 호출 성공시 */
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info("NetworkUtil : API 호출 성공");
                return readBody(con.getInputStream()); /* 성공 결과 값을 문자열로 변환 */
            } else {
                return readBody(con.getErrorStream()); /* 실패 결과 값을 문자열로 변환 */
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    /* OpenAPI URL에 접속하기
     * 이 함수는 NetworkUtil에서만 사용하기에 접근 제한자를 private로 선언함
     * 외부 자바 파일에서 호출 불가
     *
     * @param apiUrl 호출할 OpenAPI URL 주소 */

    private static HttpURLConnection connect(String apiUrl) {

        try {
            URL url = new URL(apiUrl);

            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    // OpenAPI 호출 후 받은 결과를 문자열로 변환하기
    // 이 함수는 NetworkUtil에서만 사용하기에 접근 제한자를 private로 선언함
    // 외부 자바 파일에서 호출 불가
    // @param body 읽은 결과값

    private static String readBody(InputStream body){
        InputStreamReader str = new InputStreamReader(body);

        try (BufferedReader br = new BufferedReader(str)) { /* 결과 값을 버퍼에 저장 */
            StringBuilder responseBody = new StringBuilder();

            String  line;
            while ((line = br.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}
