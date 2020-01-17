package Handlers;

import Debug.Debug;
import Json.DataBase;
import Json.GetDataClassFromJson;
import Utils.Api;
import Variables.Channels;
import Variables.Roles;
import Variables.Users;
import Variables.Variables;
import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.json.simple.parser.JSONParser;
import javax.security.auth.login.LoginException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlertHandler extends TimerTask {
    private String lastAlert = "";
    private boolean isOutdated = false;
    private boolean isMaintence = false;

    private boolean setGameOnline = false;
    private boolean pushDOUpdateAlert = false;
    private boolean pushMaintenceAlert = false;
    private boolean pushDOPEUpdateAlert = false;

    private Channels Channels = new Channels();
    private Users Users = new Users();
    private Roles Roles = new Roles();
    private Variables Variables = new Variables();
    private JDA jda;
    {
        try {
            jda = new JDABuilder("NjA5Mzk3Mjg2NzU3NDY2MTMz.XhDisg.RPZwHLLQnypA2PTSDOIl5K1cubg").build().awaitReady();
            // main - NjA5Mzk3Mjg2NzU3NDY2MTMz.XfEmZQ.W0qXjoc-MiyTC8xx8HaSYiKnmFY
            // test - NjM3NzE4NDcyNDAyNjY1NDcy.Xfpntw.rLJb4O-A_lUButzij_R7ez0GGVg
            Debug.p("AlertHandler", "JDA", "Finished Building 2 JDA!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try (Stream<Path> walk = Files.walk(Paths.get(System.getProperty("user.dir") + "/Users"))) {
            List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(".txt")).collect(Collectors.toList());

            result.forEach(warnedFile -> {
                Gson gson = new Gson();
                DataBase data = null;
                try {
                    data = gson.fromJson(readJson(warnedFile).toString(), DataBase.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Instant instant = Instant.parse(data.warnedTime);
                Duration timeElapsed = Duration.between(instant, Instant.now());

                if (timeElapsed.toHours() >= 24) {
                    jda.getGuildById(Channels.getServer()).removeRoleFromMember(jda.getGuildById(Channels.getServer()).getMember(jda.getUserById(data.ID)), jda.getRoleById(Roles.getWarned())).queue();
                    jda.getTextChannelById(Channels.getWarnedArchive()).sendMessage("Removed `Warned` role from **" + data.userName + "** after 24h.").queue();

                    try {Files.deleteIfExists(Paths.get(warnedFile));}
                    catch(NoSuchFileException e) {Debug.p("AlertHandler", "removeUserFile", "No such file/directory exists!");}
                    catch(DirectoryNotEmptyException e) {Debug.p("AlertHandler", "removeUserFile", "Directory is not empty.");}
                    catch(IOException e) {Debug.p("AlertHandler", "removeUserFile", "Invalid permissions.");}

                    Debug.p("AlertHandler", "removeUserFile", "DataBase file for " + data.userName + " successfully removed!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Api _a = new Api();
        if (this.isSetGameOnline()) {
            jda.getPresence().setActivity(Activity.playing("Online!"));
        }
        else {
            jda.getPresence().setActivity(Activity.playing("Offline!"));
        }
        if (isPushDOUpdateAlert()) {
            jda.getTextChannelById(Channels.getNews()).sendMessage("@everyone\n" +
                    "Darkorbit pushed a new update. Bot is Offline!\n" +
                    "Please be patient while the Developers are working on the update!").queue();
            pushDOUpdateAlert = false;
        }
        else if (isPushMaintenceAlert()) {
            jda.getTextChannelById(Channels.getNews()).sendMessage("@everyone\n" + lastAlert).queue();
            pushMaintenceAlert = false;
        }
        else if (isPushDOPEUpdateAlert()) {
            jda.getTextChannelById(Channels.getNews()).sendMessage("@everyone\n" + lastAlert).queue();
            pushDOPEUpdateAlert = false;
        }

        try {
            _a.update();
            if (!GetDataClassFromJson.get_data21().equals(GetDataClassFromJson.get_data24()) && this.compateLastAlert() && !isOutdated) {
                this.updateLastAlert();
                pushDOUpdateAlert = true;
                setGameOnline = false;
                isOutdated = true;
                Debug.p("AlertHandler", "DO_Update_Checker", "DO Update!");
            }
            else if (GetDataClassFromJson.get_data21().equals(GetDataClassFromJson.get_data24())) {
                setGameOnline = true;
            }

            if (GetDataClassFromJson.get_data5() != null && !isMaintence && !GetDataClassFromJson.get_data4().equals("") && this.compateLastAlert()) {
                this.updateLastAlert();
                isMaintence = true;
                pushMaintenceAlert = true;
                Debug.p("AlertHandler", "Alert_Checker", "Maintence!");
            }
            else if (!GetDataClassFromJson.get_data4().equals("") && this.compateLastAlert() && GetDataClassFromJson.get_data5() == null) {
                this.updateLastAlert();
                pushDOPEUpdateAlert = true;
                Debug.p("AlertHandler", "Alert_Checker", "Alert!");
            }

            if (GetDataClassFromJson.get_data5() == null && isMaintence)
                isMaintence = false;
            if (GetDataClassFromJson.get_data21().equals(GetDataClassFromJson.get_data24()) && isOutdated)
                isOutdated = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLastAlert() {
        lastAlert = GetDataClassFromJson.get_data4();
    }

    private boolean compateLastAlert() {
        if (!GetDataClassFromJson.get_data4().equals(lastAlert))
            return true;
        else
            return false;
    }

    public boolean isPushDOUpdateAlert() {
        return pushDOUpdateAlert;
    }

    public boolean isSetGameOnline() {
        return setGameOnline;
    }

    public boolean isPushMaintenceAlert() {
        return pushMaintenceAlert;
    }

    public boolean isPushDOPEUpdateAlert() {
        return pushDOPEUpdateAlert;
    }

    private static Object readJson(String filename) throws Exception {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        return jsonParser.parse(reader);
    }
}
