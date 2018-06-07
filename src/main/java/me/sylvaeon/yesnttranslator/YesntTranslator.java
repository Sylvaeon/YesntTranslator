package me.sylvaeon.yesnttranslator;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.Presence;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.*;

public class YesntTranslator extends ListenerAdapter {
	private static final String BOT_TOKEN = "NDQ3MTE1NTk3MTA5NTI2NTI5.DeC4mg.dMryqyU_iHel0IYKXajsly2xWsY";
	private static JDA jda;
	private static Presence presence;
	private static HashMap<String, String> translations;
	private static Guild guild;

	public static void main(String[] args) {
		initTranslations();
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(BOT_TOKEN).buildBlocking();
			jda.addEventListener(new YesntTranslator());
			presence = jda.getPresence();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}
		presence.setGame(Game.playing("Say Yesn't To Drugs"));
		guild = jda.getGuildById(446211545945473034L);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw();
		TextChannel textChannel = event.getTextChannel();
		User user = event.getAuthor();
		Member member = event.getMember();
		String[] args;
		if(!user.isBot()) {
			if(member.hasPermission(Permission.ADMINISTRATOR)) {
				if(message.startsWith("!translator")) {
					message = message.substring("!translator ".length());
					args = message.split(" ");
					if(message.startsWith("add")) {
						addTranslation(textChannel, args[1], args[2]);
					} else if(message.startsWith("edit")) {
						editTranslation(textChannel, args[1], args[2]);
					} else if(message.startsWith("remove")) {
						removeTranslation(textChannel, args[1]);
					} else if(message.startsWith("close")) {
						close();
					} else if(message.startsWith("help")) {
						textChannel.sendMessage("```Commands:\n" +
							"!translate <word> - Translates a word\n\n" +
							"Commands (Admin only): \n" +
							"!translator add <input> <output> - Adds a new translation\n" +
							"!translator edit <input> <new_output> - Edits a preexisting translation\n" +
							"!translator remove <input> - Removes a preexisting translation\n" +
							"!translator help - Shows this list\n" +
							"!translator close - Closes the bot\n" +
							"!translator save - Saves all translations\n" +
							"!translator load - Loads all translations\n" +
							"!translator list - Lists all available translations```").queue();
					} else if(message.startsWith("list")) {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("List of translations:\n");
						for (Map.Entry<String, String> entry : translations.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							stringBuilder.append(key).append(" -> ").append(value).append("\n");
						}
						PrivateChannel privateChannel = user.openPrivateChannel().complete();
						String[] messages = stringBuilder.toString().split("(?<=\\G.{1500})");
						for(String string : messages) {
							privateChannel.sendMessage("```" + string + "```").queue();
						}
					} else if(message.startsWith("save")) {
						saveTranslations();
						textChannel.sendMessage("Translations saved!").queue();
					} else if(message.startsWith("load")) {
						initTranslations();
						textChannel.sendMessage("Translations loaded!").queue();
					}
					return;
				} else if(message.startsWith("!translate")) {
					args = message.split(" ");
					String key = args[1];
					String value = translations.get(key);
					textChannel.sendMessage(format(capitalize(key)) + " -> " + format(capitalize(value))).queue();
				}
			}
		}
	}

	public void close() {
		saveTranslations();
		jda.shutdownNow();
	}

	public static void initTranslations() {
		translations = new HashMap<>();
		try {
			File dir = new File("resources");
			if(!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File("resources/translations.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			List<String> lines = new ArrayList<>();
			String line;
			while((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			fileReader.close();
			String[] parts;
			String in, out;
			for(String l : lines) {
				parts = l.split("=");
				in = parts[0];
				out = parts[1];
				translations.put(in.toLowerCase(), out.toLowerCase());
			}
			for(Map.Entry<String, String> entry : translations.entrySet()) {
				translations.put(entry.getKey(), entry.getValue().replaceAll("â€™", "'"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveTranslations() {
		try {
			File dir = new File("resources");
			if(!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File("resources/translations.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("input=output\n");
			for (Map.Entry<String, String> entry : translations.entrySet()) {
				fileWriter.append(entry.getKey() + "=" + entry.getValue());
				fileWriter.append("\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addTranslation(TextChannel textChannel, String input, String output) {
		if(!translations.containsKey(input)) {
			translations.put(input, output);
			textChannel.sendMessage("Added translation").queue();
		} else {
			textChannel.sendMessage("Translation already exists!").queue();
		}
	}

	public static void editTranslation(TextChannel textChannel, String input, String output) {
		if(translations.containsKey(input)) {
			translations.remove(input);
			translations.put(input, output);
			textChannel.sendMessage("Edited translation").queue();
		} else {
			textChannel.sendMessage("Translation doesn't exist!").queue();
		}
	}

	public static void removeTranslation(TextChannel textChannel, String input) {
		if(translations.containsKey(input)) {
			translations.remove(input);
			textChannel.sendMessage("Removed translation").queue();
		} else {
			textChannel.sendMessage("Translation doesn't exist!").queue();
		}
	}

	public static String capitalize(String input) {
		if (input == null || input.length() == 0) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	public static String format(String input) {
		if(input.equalsIgnoreCase("No")) {
			input = "The Forbidden Word Is \"N*\"";
		} else if(input.equalsIgnoreCase("Yesn't")) {
			input = "Use: \"Yesn't\" Instead\nPlease Read Our " + guild.getTextChannelById(446717129715220480L).getAsMention() + " and " + guild.getTextChannelById(446494391608279040L).getAsMention();
		}
		return input;
	}
}
