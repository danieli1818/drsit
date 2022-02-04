package drsit.utils.messages;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessagesSender {

	private String prefix;
	
	private String errorPrefix;
	
	private static MessagesSender instance;
	
	private MessagesSender(String prefix, String errorPrefix) {
		if (prefix == null) {
			prefix = "";
		}
		if (errorPrefix == null) {
			errorPrefix = "&4";
		}
		this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		this.errorPrefix = ChatColor.translateAlternateColorCodes('&', errorPrefix);
	}
	
	public static MessagesSender getInstance(String prefix, String errorPrefix) {
		if (instance == null) {
			instance = new MessagesSender(prefix, errorPrefix);
		}
		return instance;
	}
	
	public static MessagesSender getInstance(String prefix) {
		return getInstance(prefix, null);
	}
	
	public static MessagesSender getInstance() {
		return getInstance(null);
	}
	
	public void sendMessage(String message, CommandSender sender) {
		if (!isMessageNullOrBlank(message)) {
			sendMessageWithoutPrefixes(this.prefix + message, sender);
		}
	}
	
	private void sendMessageWithoutPrefixes(String message, CommandSender sender) {
		if (!isMessageNullOrBlank(message)) {
			sender.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
		}
	}
	
	private boolean isMessageNullOrBlank(String message) {
		return message == null || message.equals("");
	}
	
	public void sendMessage(String[] messages, CommandSender sender) {
		if (!areMessagesNullOrBlank(messages)) {
			sender.sendMessage(this.prefix);
			TextComponent[] messagesTextComponents = new TextComponent[messages.length];
			for (int i = 0; i < messages.length; i++) {
				messagesTextComponents[i] = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', messages[i])));
			}
			sender.spigot().sendMessage(messagesTextComponents);
		}
	}
	
	private boolean areMessagesNullOrBlank(String[] messages) {
		for (String message : messages) {
			if (!isMessageNullOrBlank(message)) {
				return false;
			}
		}
		return true;
	}
	
	public void sendMessage(BaseComponent message, CommandSender sender) {
		TextComponent newMessage = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', this.prefix + message.toPlainText())));
		newMessage.setClickEvent(message.getClickEvent());
		newMessage.setHoverEvent(message.getHoverEvent());
		sender.spigot().sendMessage(newMessage);
	}
	
	public void sendErrorMessage(String message, CommandSender sender) {
		if (!isMessageNullOrBlank(message)) {
			sendMessageWithoutPrefixes(this.errorPrefix + message, sender);
		}
	}
	
}
