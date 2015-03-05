package com.snaps.workaholics_emojikeyboard;

public class PagerStep {
	private int step;
	private boolean dirty;
	PagerStep(){
		step = 0;
		dirty = false;
	}
	public void setDirty(boolean dirty){
		this.dirty = dirty;
	}
	public boolean getDirty(){
		return dirty;
	}
	public void setStep(int step){
		this.step = step;
	}
	public int getStep(){
		return step;
	}
}
