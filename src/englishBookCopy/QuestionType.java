package englishBookCopy;

import java.util.HashMap;
import java.util.Map;

public enum QuestionType {
	DESCRIPTIVE("서술형"),
	MULTIPLE_CHOICE("객관식"),
	SELECTIVE("선택형");
	
	private static final 
	Map<String, QuestionType> qTypeCache = new HashMap<>();
	
	static {
		for(QuestionType type : QuestionType.values())
			qTypeCache.put(type.name, type);
	}
	
	private String name;
	
	private QuestionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static QuestionType parseType(String name) {
		return qTypeCache.get(name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
