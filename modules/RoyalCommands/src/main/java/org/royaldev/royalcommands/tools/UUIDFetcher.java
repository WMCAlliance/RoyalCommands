/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.tools;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<Map<String, UUID>> {

    private static final int MAX_SEARCH = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/page/";
    private static final String AGENT = "minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;

    public UUIDFetcher(List<String> names) {
        this.names = ImmutableList.copyOf(names);
    }

    @SuppressWarnings("unchecked")
    private static String buildBody(final List<String> names) {
        final List<JSONObject> lookups = new ArrayList<>();
        for (final String name : names) {
            final JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("agent", UUIDFetcher.AGENT);
            lookups.add(obj);
        }
        return JSONValue.toJSONString(lookups);
    }

    private static HttpURLConnection createConnection(final int page) throws Exception {
        final URL url = new URI(UUIDFetcher.PROFILE_URL + page).toURL();
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static void writeBody(final HttpURLConnection connection, final String body) throws Exception {
        final DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(body.getBytes());
        writer.flush();
        writer.close();
    }

    public Map<String, UUID> call() throws Exception {
        final Map<String, UUID> uuidMap = new HashMap<>();
        final String body = UUIDFetcher.buildBody(this.names);
        for (int i = 1; i < UUIDFetcher.MAX_SEARCH; i++) {
            final HttpURLConnection connection = UUIDFetcher.createConnection(i);
            UUIDFetcher.writeBody(connection, body);
            final JSONObject jsonObject = (JSONObject) this.jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            final JSONArray array = (JSONArray) jsonObject.get("profiles");
            final Number count = (Number) jsonObject.get("size");
            if (count.intValue() == 0) {
                break;
            }
            for (final Object profile : array) {
                final JSONObject jsonProfile = (JSONObject) profile;
                final String id = (String) jsonProfile.get("id");
                final String name = (String) jsonProfile.get("name");
                final UUID uuid = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
                uuidMap.put(name, uuid);
            }
        }
        return uuidMap;
    }
}
