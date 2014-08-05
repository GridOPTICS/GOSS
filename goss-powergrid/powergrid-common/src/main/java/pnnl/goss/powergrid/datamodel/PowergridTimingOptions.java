package pnnl.goss.powergrid.datamodel;

public class PowergridTimingOptions {
	public static final String TIME_OPTION_CURRENT = "currentTime";
	public static final String TIME_OPTION_OFFSET = "currentTimeOffset";
	public static final String TIME_OPTION_STATIC = "staticTime";
	
	private String timingOption;
	private String timingOptionArgument;
	
	public PowergridTimingOptions(String timingOption, String timingOptionArgument){
		this.timingOption = timingOption;
		this.timingOptionArgument = timingOptionArgument;
	}
	
	/**
	 * @return the timingOption
	 */
	public String getTimingOption() {
		return timingOption;
	}
	/**
	 * @param timingOption the timingOption to set
	 */
	public void setTimingOption(String timingOption) {
		this.timingOption = timingOption;
	}
	/**
	 * @return the timingOptionArgument
	 */
	public String getTimingOptionArgument() {
		return timingOptionArgument;
	}
	/**
	 * @param timingOptionArgument the timingOptionArgument to set
	 */
	public void setTimingOptionArgument(String timingOptionArgument) {
		this.timingOptionArgument = timingOptionArgument;
	}
	
	
}
