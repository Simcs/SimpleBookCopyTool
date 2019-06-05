package englishBookCopy;

import static englishBookCopy.QuestionType.DESCRIPTIVE;
import static englishBookCopy.QuestionType.MULTIPLE_CHOICE;
import static englishBookCopy.QuestionType.SELECTIVE;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class BookCopy extends JFrame {

	private static final long serialVersionUID = -2812449735826241402L;

	private static final Font BASIC_FONT = new Font("맑은 고딕", Font.PLAIN, 17);

	private static final String QUESTION_FILE_NAME = "question.ser";
	private static final String ELAPSED_TIME_FILE_NAME = "elapsed_time.ser";
	private static final String RESULT_FILE_NAME = "result.txt";

	private static final long TIME_RENEWAL_INTERVAL = 100L;
	
	private static final int MINIMUM_ELAPSED_TIME = 60;

	private JPanel pInfo;
	private JPanel pTypeInfo;
	private JPanel pRestInfo;
	private JPanel pWriting;
	private JScrollPane pQuestionAndExample;
	private JScrollPane pResult;
	private JPanel pButtons;

	private JLabel lblSerialNumber;
	private JLabel lblChapter;
	private JLabel lblPages;
	private JLabel lblOrder;
	private JLabel lblType;
	private JLabel lblElapsedTime;

	private JTextField txtSerialNumber;
	private JTextField txtChapter;
	private JTextField txtPages;
	private JTextField txtOrder;

	private JRadioButton btnDescriptive;
	private JRadioButton btnMultipleChoice;
	private JRadioButton btnSelective;
	private ButtonGroup group;

	private JButton btnNextQuestion;
	private JButton btnPreviousQuestion;
	private JButton btnNextPage;
	// private JButton btnNextChapter;
	private JButton btnInsert;

	private JMenuItem iSaveQuestion;
	private JMenuItem iPauseTimer;
	private JMenuItem iResumeTimer;
	private JMenuItem iInitializeTimer;
	private JMenuItem iTotalElapsedTime;

	private JMenu file;
	private JMenu timer;
	private JMenuBar jmb;

	private JTextArea areaQuestionAndExample;
	private JTextArea areaResult;

	private SimpleTimeWatch pageTimer;

	private Question curQuestion = Question.BASIC_QUESTION;

	private Map<Integer, BigDecimal> elapsedTimeForEachPage = new TreeMap<>();

	BookCopy() {
		setLayout(new BorderLayout());

		changeAllSwingComponentDefaultFont(BASIC_FONT);

		setMenuBar();
		setInfoPanel();
		setWritingPanel();
		setButtonsPanel();

		setJMenuBar(jmb);
		add(pInfo, BorderLayout.NORTH);
		add(pWriting, BorderLayout.CENTER);
		add(pButtons, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				iSaveQuestion.doClick();
				super.windowClosing(e);
			}
		});

		setTitle("문제 일련번호 찾기");
		setVisible(true);
		setBounds(100, 100, 960, 520);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		new Thread(new ElapsedTimeUpdater()).start();

		deserializeQuestion();
		setInfoPanelWithCurrentQuestion();

		deserializeElapsedTime();

		pageTimer = new SimpleTimeWatch();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					Scanner scan = new Scanner(System.in);
					System.out.println("page 1 , page2 ?");
					int page1 = scan.nextInt();
					int page2 = scan.nextInt();
					swap_page(page1, page2);
					scan.close();
				}
			}
		}).start();
	}

	private static void changeAllSwingComponentDefaultFont(Font font) {
		try {
			UIDefaults swingComponentDefaultTable = UIManager.getDefaults();
			Enumeration<Object> allDefaultKey = swingComponentDefaultTable.keys();
			while (allDefaultKey.hasMoreElements()) {
				String defaultKey = allDefaultKey.nextElement().toString();
				if (defaultKey.indexOf("font") != -1)
					UIManager.put(defaultKey, font);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void swap_page(int page1, int page2) {
		BigDecimal tmp = elapsedTimeForEachPage.get(page1);
		elapsedTimeForEachPage.put(page1, elapsedTimeForEachPage.get(page2));
		elapsedTimeForEachPage.put(page2, tmp);
	}

	private void setMenuBar() {
		iSaveQuestion = new JMenuItem("현재 문제 및 걸린 시간 저장");
		iSaveQuestion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		iSaveQuestion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentQuestionFromInfoPanel();
				serializeQuestion();
				serializeElapsedTime();
				makeResultFile();
				JOptionPane.showMessageDialog(BookCopy.this, "저장 완료");
			}
		});

		iPauseTimer = new JMenuItem("타이머 일시정지");
		iPauseTimer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		iPauseTimer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				pageTimer.pause();
				disablePause();
			}
		});

		iResumeTimer = new JMenuItem("타이머 일시정지 해제");
		iResumeTimer.setEnabled(false);
		iResumeTimer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		iResumeTimer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				pageTimer.resume();
				enablePause();
			}
		});

		iInitializeTimer = new JMenuItem("타이머 초기화");
		iInitializeTimer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		iInitializeTimer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// reset timer
				pageTimer = new SimpleTimeWatch();
				enablePause();
			}
		});

		iTotalElapsedTime = new JMenuItem("총 걸린시간");
		iTotalElapsedTime.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		iTotalElapsedTime.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String totalTime = SimpleTimeWatch.getFormalTimeString(getTotalElaspedTime());
				JOptionPane.showMessageDialog(BookCopy.this, "총 걸린 시간은 " + totalTime + " 입니다");
				printAllElapsedTime();
			}
		});

		file = new JMenu("파일");
		timer = new JMenu("타이머");
		jmb = new JMenuBar();

		file.add(iSaveQuestion);
		file.add(iTotalElapsedTime);
		timer.add(iPauseTimer);
		timer.add(iResumeTimer);
		timer.add(iInitializeTimer);

		jmb.add(file);
		jmb.add(timer);
	}

	private void disablePause() {
		iPauseTimer.setEnabled(false);
		iResumeTimer.setEnabled(true);
	}

	private void enablePause() {
		iPauseTimer.setEnabled(true);
		iResumeTimer.setEnabled(false);
	}

	private void setInfoPanel() {
		pInfo = new JPanel(new GridLayout(3, 1));
		pRestInfo = new JPanel();
		pTypeInfo = new JPanel();

		lblSerialNumber = new JLabel("일련번호 : ");
		lblChapter = new JLabel("단원명 : ");
		lblPages = new JLabel("페이지 : ");
		lblOrder = new JLabel("순서 : ");
		lblType = new JLabel("유형 : ");
		lblElapsedTime = new JLabel("걸린 시간 : ", JLabel.RIGHT);

		txtSerialNumber = new JTextField(5);
		txtChapter = new JTextField(20);
		txtPages = new JTextField(5);
		txtOrder = new JTextField(5);

		btnDescriptive = new JRadioButton(DESCRIPTIVE + "(Q)");
		btnDescriptive.setMnemonic(KeyEvent.VK_Q);
		btnDescriptive.setName(DESCRIPTIVE.getName());
		btnMultipleChoice = new JRadioButton(MULTIPLE_CHOICE + "(W)");
		btnMultipleChoice.setMnemonic(KeyEvent.VK_W);
		btnMultipleChoice.setName(MULTIPLE_CHOICE.getName());
		btnSelective = new JRadioButton(SELECTIVE + "(E)");
		btnSelective.setMnemonic(KeyEvent.VK_E);
		btnSelective.setName(SELECTIVE.getName());

		group = new ButtonGroup();

		group.add(btnDescriptive);
		group.add(btnMultipleChoice);
		group.add(btnSelective);

		pRestInfo.add(lblSerialNumber);
		pRestInfo.add(txtSerialNumber);
		pRestInfo.add(lblChapter);
		pRestInfo.add(txtChapter);
		pRestInfo.add(lblPages);
		pRestInfo.add(txtPages);
		pRestInfo.add(lblOrder);
		pRestInfo.add(txtOrder);

		pTypeInfo.add(lblType);
		pTypeInfo.add(btnDescriptive);
		pTypeInfo.add(btnMultipleChoice);
		pTypeInfo.add(btnSelective);

		pInfo.add(pRestInfo);
		pInfo.add(pTypeInfo);
		pInfo.add(lblElapsedTime);
	}

	private void setWritingPanel() {
		pWriting = new JPanel(new GridLayout(2, 1));

		pQuestionAndExample = new JScrollPane();
		areaQuestionAndExample = new JTextArea();
		pQuestionAndExample.setViewportView(areaQuestionAndExample);
		pQuestionAndExample.setBorder(new TitledBorder(new LineBorder(Color.lightGray), "반복되는 문제 & 보기"));

		pResult = new JScrollPane();
		areaResult = new JTextArea();
		pResult.setViewportView(areaResult);
		pResult.setBorder(new TitledBorder(new LineBorder(Color.lightGray), "결과"));

		pWriting.add(pQuestionAndExample);
		pWriting.add(pResult);
	}

	private void setButtonsPanel() {
		pButtons = new JPanel();

		btnNextQuestion = new JButton("다음 문제(N)");
		btnNextQuestion.setMnemonic(KeyEvent.VK_N);
		btnNextQuestion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentQuestionFromInfoPanel();
				curQuestion = new Question(curQuestion.getSerialNumber(), curQuestion.getChapter(),
						curQuestion.getPages(), curQuestion.getOrder() + 1, curQuestion.getqType());
				areaResult.setText("");
				setInfoPanelWithCurrentQuestion();
			}
		});

		btnPreviousQuestion = new JButton("이전 문제(B)");
		btnPreviousQuestion.setMnemonic(KeyEvent.VK_B);
		btnPreviousQuestion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentQuestionFromInfoPanel();
				curQuestion = new Question(curQuestion.getSerialNumber(), curQuestion.getChapter(),
						curQuestion.getPages(), curQuestion.getOrder() - 1, curQuestion.getqType());
				areaResult.setText("");
				setInfoPanelWithCurrentQuestion();
			}
		});

		btnNextPage = new JButton("다음 페이지(M)");
		btnNextPage.setMnemonic(KeyEvent.VK_M);
		btnNextPage.addActionListener(new ActionListener() {

			// 이미 있으면 새로 시간 추가, 없으면 기존값에 더해서 갱신
			private void putOrIncrementElapsedTime(BigDecimal elapsedTime) {
				if (elapsedTimeForEachPage.containsKey(curQuestion.getPages())) {
					elapsedTimeForEachPage.put(curQuestion.getPages(),
							elapsedTimeForEachPage.get(curQuestion.getPages()).add(elapsedTime));
				} else {
					elapsedTimeForEachPage.put(curQuestion.getPages(), elapsedTime);
				}

				// elapsedTimeForEachPage.computeIfPresent(curQuestion.getPages(), (k, v) ->
				// v.add(elapsedTime));
				// elapsedTimeForEachPage.putIfAbsent(curQuestion.getPages(), elapsedTime);

				printCurrentElapsedTime();
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentQuestionFromInfoPanel();

				BigDecimal elapsedTime = pageTimer.getElapsedTime();
				String elapsedTimeString = SimpleTimeWatch.getFormalTimeString(elapsedTime);
				int answer = JOptionPane.showConfirmDialog(BookCopy.this,
						"해당 페이지 완성에 걸린 시간은 " + elapsedTimeString + " 입니다.\n입력하실려면 (예)");
				if (answer != JOptionPane.OK_OPTION)
					return;

				putOrIncrementElapsedTime(elapsedTime);

				// 타이머 초기화
				iInitializeTimer.doClick();

				curQuestion = new Question(curQuestion.getSerialNumber(), curQuestion.getChapter(),
						curQuestion.getPages() + 1, 1, curQuestion.getqType());
				areaResult.setText("");
				setInfoPanelWithCurrentQuestion();
			}
		});

		btnInsert = new JButton("입력(I)");
		btnInsert.setMnemonic(KeyEvent.VK_I);
		btnInsert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentQuestionFromInfoPanel();
				areaResult.setText("");
				areaResult.append(curQuestion + "\n\n");
				areaResult.append(areaQuestionAndExample.getText());
				areaResult.requestFocus();
			}
		});

		pButtons.add(btnNextQuestion);
		pButtons.add(btnPreviousQuestion);
		pButtons.add(btnNextPage);
		pButtons.add(btnInsert);
	}

	private void setCurrentQuestionFromInfoPanel() {
		String serialNumber = txtSerialNumber.getText();
		String chapter = txtChapter.getText();
		int pages = Integer.parseInt(txtPages.getText());
		int order = Integer.parseInt(txtOrder.getText());
		QuestionType qType = null;
		for (Enumeration<AbstractButton> types = group.getElements(); types.hasMoreElements();) {
			JRadioButton btn = (JRadioButton) types.nextElement();
			if (btn.isSelected())
				qType = QuestionType.parseType(btn.getName());
		}

		curQuestion = new Question(serialNumber, chapter, pages, order, qType);
	}

	private void setInfoPanelWithCurrentQuestion() {
		txtSerialNumber.setText(curQuestion.getSerialNumber() + "");
		txtChapter.setText(curQuestion.getChapter());
		txtPages.setText(String.format("%03d", curQuestion.getPages()));
		txtOrder.setText(curQuestion.getOrder() + "");

		for (Enumeration<AbstractButton> types = group.getElements(); types.hasMoreElements();) {
			JRadioButton btn = (JRadioButton) types.nextElement();
			if (btn.getName() == curQuestion.getqType().getName())
				btn.setSelected(true);
		}
	}

	private BigDecimal getTotalElaspedTime() {
		BigDecimal totalElapsedTime = BigDecimal.ZERO;
		for (BigDecimal time : elapsedTimeForEachPage.values())
			if (time.intValue() > MINIMUM_ELAPSED_TIME)
				totalElapsedTime = totalElapsedTime.add(time);
		
		return totalElapsedTime;
	}

	private BigDecimal getAverageElaspedTime() {
		BigDecimal averageElapsedTime = BigDecimal.ZERO;
		int effectiveSize = 0;
		for (BigDecimal time : elapsedTimeForEachPage.values())
			if (time.intValue() > MINIMUM_ELAPSED_TIME)
				effectiveSize++;
		
		if (effectiveSize > 0)
			averageElapsedTime = getTotalElaspedTime()
					.divide(new BigDecimal(String.valueOf(effectiveSize)), 1, RoundingMode.CEILING);
		
		return averageElapsedTime;
	}

	private void makeResultFile() {
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(RESULT_FILE_NAME));
			for (Map.Entry<Integer, BigDecimal> entry : elapsedTimeForEachPage.entrySet()) {
				if(entry.getValue().intValue() < MINIMUM_ELAPSED_TIME)
					continue;
				
				int page = entry.getKey();
				String elapsedTime = SimpleTimeWatch.getFormalTimeString(entry.getValue());
				fw.write(page + "쪽에서 걸린 시간 : " + elapsedTime + "\r\n");
			}
			fw.write("\r\n");
			fw.write("총 걸린 시간 : " + SimpleTimeWatch.getFormalTimeString(getTotalElaspedTime()) + "\r\n");
			fw.write("페이지 당 걸린 평균 시간 : " + SimpleTimeWatch.getFormalTimeString(getAverageElaspedTime()) + "\r\n");

		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private void serializeElapsedTime() {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ELAPSED_TIME_FILE_NAME)));

			out.writeObject(elapsedTimeForEachPage);
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private void deserializeElapsedTime() {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(ELAPSED_TIME_FILE_NAME))));

			@SuppressWarnings("unchecked")
			Map<Integer, BigDecimal> tmp = (TreeMap<Integer, BigDecimal>) (in.readObject());
			elapsedTimeForEachPage = tmp;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private void serializeQuestion() {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(QUESTION_FILE_NAME)));

			out.writeObject(curQuestion);
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private void deserializeQuestion() {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(QUESTION_FILE_NAME)));

			curQuestion = new Question((Question) in.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private void printCurrentElapsedTime() {
		BigDecimal currentElapsedTime = elapsedTimeForEachPage.get(curQuestion.getPages());
		System.out.println(curQuestion.getPages() + "쪽에서 걸린 시간 : " + currentElapsedTime);
	}

	private void printAllElapsedTime() {
		elapsedTimeForEachPage.forEach((page, elapsedTime) -> {
			System.out.println(page + "쪽에서 걸린 시간 : " + elapsedTime);
		});
	}

	class ElapsedTimeUpdater implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(TIME_RENEWAL_INTERVAL);
					String elapsedTime = SimpleTimeWatch.getFormalTimeString(pageTimer.getElapsedTime());
					lblElapsedTime.setText("걸린 시간 : " + elapsedTime + "   ");
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	public static void main(String[] args) {
		new BookCopy();
	}
}
