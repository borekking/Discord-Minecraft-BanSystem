package de.borekking.banSystem.util.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MinecraftUUIDUtils {

    public static class NoSuchPlayerException extends Exception {
        public NoSuchPlayerException() {
        }
    }

    private static final String API_URL, SESSION_SERVER_URL;

    static {
        SESSION_SERVER_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
        API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    }

    public static UUID getUUIDFromName(String name) throws NoSuchPlayerException {
        String uuid = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            uuid = (((JsonObject)new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();
        } catch (Exception e) {
//            System.out.println("Unable to get UUID of: " + name + "!");
//            uuid = "er";
            throw new NoSuchPlayerException();
//            return null;
        }
        return MinecraftUUIDUtils.getUUID(uuid);
    }

    public static String readInputStreamAsString(InputStream in)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while(result != -1) {
            byte b = (byte)result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    public static UUID getUUIDFromPlayerName(String playerName) throws NoSuchPlayerException {
        Gson gson = new Gson();
        String url = API_URL + playerName;
        String json = getStringFromURL(url);

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        if (jsonObject == null) throw new NoSuchPlayerException();
        String uuid = jsonObject.get("id").getAsString();
        return getUUID(getRealUUIDString(uuid));
    }

    public static UUID getUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    public static String getNameFromUUID(UUID uuid) {
        Gson gson = new Gson();

        String unrealUUID = getUnRealUUIDString(uuid.toString());
        String url = SESSION_SERVER_URL + unrealUUID;

        String json = getStringFromURL(url);
        JsonObject mainObject = gson.fromJson(json, JsonObject.class);

        return mainObject.get("name").getAsString();
    }

    private static String getRealUUIDString(String uuid) {
        String[] parts = new String[5];
        parts[0] = uuid.substring(0, 8);
        parts[4] = uuid.substring(20);
        for (int i = 8, index = 1; index <= 3; index++, i += 4)
            parts[index] = uuid.substring(i, i + 4);
        return String.join("-", parts);
    }

    private static String getUnRealUUIDString(String uuid) {
        return uuid.replaceAll("[-]", "");
    }

    private static String getStringFromURL(String url) {
        StringBuilder text = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new URL(url).openStream());
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                while (line.startsWith(" ")) {
                    line = line.substring(1);
                }
                text.append(line);
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}

