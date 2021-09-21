package com.emmad.mcdiscord;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

public class DiscordWebhook {
    private static final String URL = "https://discord.com/api/webhooks/888537014268485722/Qg-8swhzjU2lmHYrfyRV-aSWBz7_Tm4KCgZz2ABlukiLQw0Lfd7ke7DQcIJJz9JEwYf3";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void saveCoordinate(CoordinateManager.Coordinate coordinate) throws RequestFailedException {
        try {
            // create request body
            DiscordReqBody discordReqBody =
                    new DiscordReqBody(coordinate.toDiscordString());
            String jsonStr = mapper.writeValueAsString(discordReqBody);
            RequestBody body = RequestBody.create(jsonStr, JSON);

            // send request
            Request request = new Request.Builder().url(URL).post(body).build();
            Response response = client.newCall(request).execute();
            response.close();

            if (!(response.isSuccessful())) {
                throw new Exception("Unsuccessful response code: " + response.code());
            }
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }
    }

    private static class DiscordReqBody {
        public String content;

        public DiscordReqBody(String content) {
            this.content = content;
        }
    }

    public static class RequestFailedException extends Exception {
        public RequestFailedException(String message) {
            super(message);
        }
    }
}
