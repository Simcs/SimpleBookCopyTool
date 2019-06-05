package englishBookCopy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SimpleTimeWatch {

	private static final BigDecimal NS_PER_SECOND = new BigDecimal(String.valueOf(Math.pow(10, 9)));
	private static final BigDecimal SECOND_PER_MINUTE = new BigDecimal("60");
	private static final BigDecimal SECOND_PER_HOUR = new BigDecimal("3600");
	private static final int DEFAULT_SCALE = 1;
	
	private long startTime;
	private long stopTime;
	private long elapsedTime;
	
	private boolean isPaused;

	SimpleTimeWatch() {
		startTime = System.nanoTime();
		elapsedTime = 0L;
		isPaused = false;
	}
	
	SimpleTimeWatch(BigDecimal start) {
		this();
		elapsedTime = start.multiply(NS_PER_SECOND).longValue();
	}
	
	//�־��� �ð��� '00�� 00��' �� ���� �������� �ٲ㼭 ��ȯ
	public static String getFormalTimeString(BigDecimal time) {
		BigDecimal[] hourAndRemainder = time.divideAndRemainder(SECOND_PER_HOUR);
		BigDecimal[] minuteAndRemainder = hourAndRemainder[1].divideAndRemainder(SECOND_PER_MINUTE);
		
		int hour = hourAndRemainder[0].intValue();
		int minute = minuteAndRemainder[0].intValue();
		BigDecimal second = minuteAndRemainder[1];
		
		StringBuilder res = new StringBuilder();
		res.append(hour > 0 ? hour + "�ð� " : "");
		res.append(minute > 0 ? minute + "�� " : "");
		res.append(second + "��");
		return res.toString();
	}

	private static BigDecimal nanoTimeToRealTime(long nanoTime) {
		BigDecimal nTime = new BigDecimal(String.valueOf(nanoTime));
		return nTime.divide(NS_PER_SECOND, DEFAULT_SCALE, RoundingMode.HALF_UP);
	}

	public void pause() {
		if (isPaused)
			return;
		isPaused = true;
		stopTime = System.nanoTime();
	}

	public void resume() {
		if (!isPaused)
			return;
		isPaused = false;
		startTime = System.nanoTime();
	}

	public BigDecimal getElapsedTime() {
		updateElapsedTime();
		return nanoTimeToRealTime(elapsedTime);
	}

	private void updateElapsedTime() {
		if(isPaused) {
			elapsedTime += stopTime - startTime;
			startTime = stopTime;
		} else {
			elapsedTime += System.nanoTime() - startTime;
			startTime = System.nanoTime();
		}
	}
}
