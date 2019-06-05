package englishBookCopy;

import static englishBookCopy.QuestionType.SELECTIVE;

import java.io.Serializable;

public class Question implements Serializable {
	
	private static final long serialVersionUID = 7945233493729680163L;

	public static final Question BASIC_QUESTION =
			new Question("일련번호", "챕터", 1, 1, SELECTIVE);
	
	private String serialNumber;
	private String chapter;
	private int pages;
	private int order;
	private QuestionType qType;
	
	public Question(String serialNumber, String chapter, int pages, 
			int order, QuestionType qType) {
		if(qType == null) 
			throw new IllegalArgumentException();
		this.serialNumber = serialNumber;
		this.chapter = chapter;
		this.pages = pages;
		this.order = order;
		this.qType = qType;
	}
	
	public Question(Question q) {
		this(q.serialNumber, q.chapter, q.pages, q.order, q.qType);
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getChapter() {
		return chapter;
	}

	public int getPages() {
		return pages;
	}

	public int getOrder() {
		return order;
	}

	public QuestionType getqType() {
		return qType;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(serialNumber);
		sb.append("-");
		sb.append(chapter);
		sb.append("-");
		sb.append(String.format("%03d", pages));
		sb.append("-");
		sb.append(order);
		sb.append(" / ");
		sb.append(qType);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(BASIC_QUESTION);
	}
}
