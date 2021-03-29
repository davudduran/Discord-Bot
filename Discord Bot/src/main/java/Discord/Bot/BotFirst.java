package Discord.Bot;
//Müzik botu için lavaplayer

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;

public class BotFirst extends ListenerAdapter {
	
	public static TextChannel announcementChannel = null;
	public static String discordToken = "Discord Token here";
	public static String twitchToken 	= "Twitch Token here";
	public static String twitchTokenSecret = "Twitch Secret Token Here";
	public static String youtubeToken = "YouTube Tokten Here";

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		System.out.println("Bot started");
		TwitchInit();
		YouTubeInit();
		DiscordInit();
	}
	public static void DiscordInit() throws LoginException {
		@SuppressWarnings("unused")
		JDA jda = JDABuilder.createDefault(discordToken)
			.addEventListeners(new BotFirst())
			.setActivity(Activity.playing("with Discord API"))
			.setAutoReconnect(true)
			.build();
		System.out.println("Discord Bot started");
	}
	
	public static void TwitchInit() {
		
		//============================T W I T C H===============================================
		TwitchClient tc = TwitchClientBuilder.builder()
				.withClientId(twitchToken)
				.withClientSecret(twitchTokenSecret)
				.withDefaultEventHandler(SimpleEventHandler.class)
	            .withEnableHelix(true)
	            .build();
		tc.getClientHelper().enableStreamEventListener("mericdem");
		System.out.println("Twitch Bot Started");
		
		tc.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoLiveEvent.class, event -> {
			announcementChannel.sendMessage("Mert yayinda!").queue();
			System.out.println(event.getChannel().getName() + " went live with title " + event.getStream().getTitle() + " on game " + event.getStream().getGameId() + "!");
		});
		
		tc.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoOfflineEvent.class ,event -> {
			System.out.println(event.getChannel().getName() + " just went offline!");
		});
		
	}
	
	public static void YouTubeInit() throws GeneralSecurityException, IOException {
		//===================================G O O G L E=======================================
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		YouTube youtube = new YouTube
				.Builder(httpTransport, JacksonFactory.getDefaultInstance(), null)
				.setApplicationName("MericBot")
				.build();
		System.out.println("YouTube Bot started");
		
		List<String> vIDs = new ArrayList<String>();
		
		YouTube.Search.List request = youtube.search().list("snippet");
		SearchListResponse response = request.setKey(youtubeToken).setChannelId("UCYVrb9zec2nUjcrKPCSOemw").setOrder("date").setType("video").execute();
		List<SearchResult> searches = response.getItems();
		
		for(SearchResult i : searches) {
			vIDs.add(i.getId().getVideoId());
		}
		Timer t = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				
				YouTube.Search.List request = null;
				try {
					 request = youtube.search().list("snippet");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				SearchListResponse response = null;
				try {
					response = request.setKey(youtubeToken).setChannelId("UCYVrb9zec2nUjcrKPCSOemw").setMaxResults(1L).setOrder("date").setType("video").execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
				List<SearchResult> searches = response.getItems();
				if(searches.size()!=0) {
					if(searches.get(0)!=null) {
						String id = searches.get(0).getId().getVideoId();
						if(!vIDs.contains(id)) {
							if(announcementChannel!=null) {
								announcementChannel.sendMessage("Yeni video var hebele "+announcementChannel.getGuild().getPublicRole().getAsMention()).queue();
							}
							vIDs.add(id);
						}
					}
				}
				
			}
		};
		t.schedule(tt, 900000,900000); //Schedule new event for every 15 minute
	}
	
	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if(event.isFromType(ChannelType.PRIVATE)) return;
		if(event.getAuthor().isBot()) return;
		JDA jda = event.getJDA();
		User u = event.getAuthor();
		Message m = event.getMessage();
		Member mem = event.getMember();
		Guild g = event.getGuild();
		MessageChannel mc = event.getChannel();
		if(announcementChannel==null)
			announcementChannel = g.getTextChannelById("805421299294797825");
		mc.sendMessage("Komut odasi " + g.getTextChannelById("805421299294797825").getAsMention() + " olarak seçildi").queue();
		
		String[] msgRaw = m.getContentDisplay().split(" ");
		//--------------------------------------------------
        String[] msg = Arrays.stream(msgRaw)
                .filter(value ->
                        value != null && value.length() > 0
                )
                .toArray(size -> new String[size]);
        //---------------------------------------------------
       
        if(msg[0].toLowerCase().equals(".change")) {
        	String playingwith="";
        	for(int i=1;i<msg.length;i++) {
        		playingwith+=msg[i]+" ";
        	}
        	jda.getPresence().setActivity(Activity.playing(playingwith));
        }
        
        if(msg[0].toLowerCase().equals(".test")) {
        	mc.sendMessage("Test Command works properly.").queue();
        }
        
        else if(msg[0].toLowerCase().equals(".privateMessage")) {
        	u.openPrivateChannel().complete().sendMessage("test message").queue();
        }
        //------------CLOSING-SEQUENCE--------------------------
        else if(msg[0].toLowerCase().equals(".close")) {
            mc.sendMessage("Bot Successfully Closed.").complete();
            
            System.out.print("Bot closed.");
            
            jda.shutdownNow();
            OkHttpClient client = jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdown();
        }
        
        /*---------------HIGHEST HIERARCHY COMMAND----------------
        else if(!g.getSelfMember().canInteract(me)) {
        	mc.sendMessage("Your hierarchy is higher than me").queue();
        	}
        //-------------------------------------------------------*/
        
	}
	
	public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
		if(event.getAuthor().isBot()) return;
		MessageChannel mc = event.getChannel();
		mc.sendMessage("Under Maintenance\nPLEASE COME BACK LATER").queue();
	}

}
