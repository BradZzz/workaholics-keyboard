package com.snaps.workaholics_emojikeyboard;

public class EmojiStore {
	private String category, asset_path, path;
	EmojiStore(String category, String asset_path, String path) {
		this.category = category;
		this.asset_path = asset_path;
		this.path = path;
	}
	public String getCategory(){
		return category;
	}
	public String getAssetPath(){
		return asset_path;
	}
	public String getPath(){
		return path;
	}
}
