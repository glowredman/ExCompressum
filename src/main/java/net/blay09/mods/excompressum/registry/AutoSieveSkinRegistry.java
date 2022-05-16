package net.blay09.mods.excompressum.registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.blay09.mods.excompressum.ExCompressum;

public class AutoSieveSkinRegistry {

    private static final Random random = new Random();
    private static final List<WhitelistEntry> availableSkins = new ArrayList<>();

    public static void load() {
        if (!ExCompressum.skipAutoSieveSkins) {
            availableSkins.clear();
            Thread loadAutoSieveSkins = new Thread(() -> {
                try {
                    URL remoteURL = new URL(ExCompressum.autoSieveSkinsURL);
                    InputStream in = remoteURL.openStream();
                    Gson gson = new Gson();
                    JsonReader reader = new JsonReader(new InputStreamReader(in));
                    List<WhitelistEntry> result = gson.fromJson(reader, new TypeToken<List<WhitelistEntry>>(){}.getType());
                    synchronized (availableSkins) {
                        availableSkins.addAll(result);
                    }
                    reader.close();
                } catch (Throwable e) { // Screw it, let's just be overprotective.
                    ExCompressum.logger.error("Could not load remote skins for auto sieve: ");
                    e.printStackTrace();
                }
            });
            loadAutoSieveSkins.setName("ExCompressum Skin Download");
            loadAutoSieveSkins.start();
        }
    }

    @Nullable
    public static WhitelistEntry getRandomSkin() {
        synchronized (availableSkins) {
            if (availableSkins.isEmpty()) {
                return null;
            }

            return availableSkins.get(random.nextInt(availableSkins.size()));
        }
    }
    
    public static class WhitelistEntry {
        
        private UUID uuid;
        private String name;
        
        public UUID getUuid() {
            return this.uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }

}