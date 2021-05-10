package com;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.junit.Assert.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DropboxTests {

    private static final String TOKEN =
            "XNyJqELY0zIAAAAAAAAAAWgqLSKhElTdQPfKs2sufdL_2oMazockDUcC2elUBWKR";
    private static final String UPLOAD_FILE_PATH = "com/testdata.txt";
    private static final String DROPBOX_FILE_PATH = "/testdata.txt";

    @Test
    @Order(1)
    public void uploadFile() {
        File data = new File(getClass().getClassLoader().getResource(UPLOAD_FILE_PATH).getFile());
        Response response = given().
                config(RestAssured.config().encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Dropbox-API-Arg",
                        "{\"mode\": \"add\"," +
                                "\"autorename\": true," +
                                "\"mute\": false," +
                                "\"path\":" + "\"" + DROPBOX_FILE_PATH + "\"," +
                                "\"strict_conflict\": false}").
                header("Content-Type", "application/octet-stream").
                body(data).
                when().post("https://content.dropboxapi.com/2/files/upload");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(2)
    public void getFileMetadata() {
        JSONObject requestForId = new JSONObject();
        requestForId.put("path", DROPBOX_FILE_PATH);
        requestForId.put("include_media_info", false);
        requestForId.put("include_deleted", false);
        requestForId.put("include_has_explicit_shared_members", false);
        Response responseWithId = RestAssured.given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Content-Type", "application/json").
                body(requestForId.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/files/get_metadata");
        String fileId = responseWithId.jsonPath().get("id");

        JSONObject requestBody = new JSONObject();
        requestBody.put("file", fileId);
        requestBody.put("actions", new ArrayList());
        Response response = RestAssured.given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Content-Type", "application/json").
                body(requestBody.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/sharing/get_file_metadata");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void deleteFile() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("path", DROPBOX_FILE_PATH);
        Response response = given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Content-Type", "application/json").
                body(requestBody.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/files/delete_v2");
        assertEquals(200, response.getStatusCode());
    }
}
