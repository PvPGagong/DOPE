package Utils;

import Debug.Debug;
import Handlers.AlertHandler;
import Handlers.CommandHandler;
import Json.GetDataClassFromJson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;

public class Api extends ListenerAdapter {

    private final OkHttpClient httpClient = new OkHttpClient();
    private JDA jda = null;
    private CreateTag Tag = new CreateTag();

    public void buildJDA() {
        try {
            try {
                jda = new JDABuilder("NjA5Mzk3Mjg2NzU3NDY2MTMz.XhDisg.RPZwHLLQnypA2PTSDOIl5K1cubg")
                        // main - NjA5Mzk3Mjg2NzU3NDY2MTMz.XfEmZQ.W0qXjoc-MiyTC8xx8HaSYiKnmFY
                        // test - NjM3NzE4NDcyNDAyNjY1NDcy.Xfpntw.rLJb4O-A_lUButzij_R7ez0GGVg
                        .addEventListeners(new Api())
                        .setActivity(Activity.playing("Online!"))
                        .build();
            } catch (LoginException e) {
                e.printStackTrace();
            }
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Debug.p("API", "JDA", "Finished Building JDA!");
            this.update();
            Timer task = new Timer();
            task.schedule(new AlertHandler(), 0,1000 * 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTextChannelCreate(final TextChannelCreateEvent e) {
        String ID;
        String ticketID = e.getChannel().getName().toString().split("ticket-")[1];
        for (Member m : e.getChannel().getMembers()) {
            String id = m.getUser().getId().toString();
            if (!id.equals("396067257760874496") &&
                    !id.equals("173743111023886336") &&

                    !id.equals("140422565393858560") &&

                    !id.equals("271686004035813387") &&
                    !id.equals("334354840438439938") &&
                    !id.equals("323058900771536898") &&
                    !id.equals("555366880118964225") &&
                    !id.equals("492651702542139433") &&
                    !id.equals("380786597458870282") &&
                    !id.equals("210538514725470208") &&
                    !id.equals("235114392482480139") &&

                    !id.equals("186962675287195648") &&
                    !id.equals("382933761911947269") &&

                    !id.equals("206781133596262401") &&
                    !id.equals("270647751941947393") &&
                    !id.equals("243041485929447424") &&
                    !id.equals("213776814198226945") &&
                    !id.equals("284636251288502285") &&
                    !id.equals("424511943055900673") &&
                    !id.equals("289168259482386442") &&

                    //fix bots
                    !id.equals("609397286757466133") &&
                    !id.equals("637718472402665472") &&
                    !id.equals("508391840525975553") &&
                    !id.equals("294882584201003009"))
            {
                ID = id;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date(System.currentTimeMillis());
                e.getChannel().sendMessage(
                        "Hello, " + Tag.asMember(ID) + "!\n\n" +
                                "You open a new ticket:\n" +
                                "`ID: " + ticketID + "`\n" +
                                "`Time: " + formatter.format(date) + "`\n" +
                                "`Creator: " + e.getJDA().getUserById(id).getName().toString() + " | " + e.getJDA().getUserById(id).getId().toString() + "`\n\n" +
                                "> Support will be with You shortly (You also can tag any support to help You faster).\n" +
                                "> Please provide us with as much information as possible so that we can solve Your problem faster.\n" +
                                "> If possible - attach **screenshots**, **GIF** or **DOPE Logs** (DOPE Logs path: `%appdata%\\DOPE\\Logs`).\n\n" +
                                "To close this ticket write **&close**."
                ).queue();
            }
        }
    }

    public void update() throws IOException {

        Request request = new Request.Builder()
                .url("https://powerofdark.space/api/status")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        this.serverUnaviableMethod();
                        throw new IOException("Unexpected code " + response);
                    }
                    String apiData = responseBody.string();
                    //Debug.p("API", "onResponse", apiData);
                    GetDataClassFromJson.parser(apiData);
                }
            }
            private void serverUnaviableMethod() {
                Request request = new Request.Builder()
                        .url("https://raw.githubusercontent.com/Gagong/Toshinou-Revamped/master/status.json")
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                            String apiData = responseBody.string();
                            //Debug.p("API", "onResponse", apiData);
                            GetDataClassFromJson.parser(apiData);
                        }
                    }
                });
            }
        });
    }

    public void onMessageReceived(MessageReceivedEvent event)
    {
        JDA jda = event.getJDA();
        User author = event.getAuthor();
        Message message = event.getMessage();
        String msg = message.getContentDisplay();
        boolean bot = author.isBot();
        CommandHandler handler = new CommandHandler();

        try {
            handler.CommandHandler(msg, event, jda);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (event.getChannel().getName().toString().contains("ticket") && author.isBot() && author.getId().toString().contains("508391840525975553")) {
            message.delete().queue();
        }

        if (event.isFromType(ChannelType.TEXT) && !bot)
        {
            Guild guild = event.getGuild();
            TextChannel textChannel = event.getTextChannel();
            Member member = event.getMember();
            String name;
            if (message.isWebhookMessage())
            {
                name = author.getName();
            }
            else
            {
                name = member.getEffectiveName();
            }
            String createChatString = guild.getName() + " | " + textChannel.getName() + " | " + name + " | " + msg;
            Debug.p("GUILD CHAT", "MessageReceive", createChatString);
        }
        else if (event.isFromType(ChannelType.PRIVATE) && !bot)
        {
            PrivateChannel privateChannel = event.getPrivateChannel();
            String createChatString = author.getName() + " | " + msg;
            Debug.p("PRIVATE CHAT", "MessageReceive", createChatString);
        }
    }
}

