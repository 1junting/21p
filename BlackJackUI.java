import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// BlackJackUI 是整個 21 點遊戲視窗，繼承 JFrame 才能建立 Swing 視窗。
public class BlackJackUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // 牌堆，負責存放還沒有被抽走的牌。
    private final ArrayList<Card> deck = new ArrayList<>();
    // 玩家目前手上的牌。
    private final ArrayList<Card> playerCards = new ArrayList<>();
    // 莊家目前手上的牌。
    private final ArrayList<Card> dealerCards = new ArrayList<>();

    // 顯示莊家卡牌圖片的面板。
    private final JPanel dealerCardPanel;
    // 顯示玩家卡牌圖片的面板。
    private final JPanel playerCardPanel;

    // 顯示莊家點數的文字。
    private final JLabel dealerScoreLabel;
    // 顯示玩家點數的文字。
    private final JLabel playerScoreLabel;
    // 顯示剩餘牌數的文字。
    private final JLabel deckCountLabel;
    // 顯示目前遊戲結果或提示訊息的文字。
    private final JLabel resultLabel;
    // 顯示對局統計資料的文字。
    private final JLabel statsLabel;

    // 玩家按下後會多抽一張牌。
    private final JButton hitButton;
    // 玩家按下後會停牌，換莊家行動。
    private final JButton standButton;
    // 一局結束後按下可以進入下一局。
    private final JButton nextRoundButton;

    // 判斷目前這局是否已經結束。
    private boolean gameOver = false;
    // 判斷莊家是否正在慢慢翻牌或補牌。
    private boolean dealerTurnInProgress = false;
    // 判斷莊家的底牌是否已經翻開。
    private boolean dealerCardsVisible = false;
    // Swing Timer 用來讓莊家每隔一段時間才做下一個動作。
    private Timer dealerTimer;

    // 已完成的總局數。
    private int totalRounds = 0;
    // 玩家贏的局數。
    private int playerWins = 0;
    // 莊家贏的局數，也就是玩家輸的局數。
    private int dealerWins = 0;
    // 平手局數。
    private int ties = 0;
    // 玩家爆牌次數。
    private int playerBusts = 0;
    // 莊家爆牌次數。
    private int dealerBusts = 0;

    // 主要文字使用的字型。
    private final Font mainFont = new Font("PingFang TC", Font.BOLD, 18);
    // 標題使用的字型。
    private final Font titleFont = new Font("PingFang TC", Font.BOLD, 30);

    // 桌面背景色。
    private final Color tableGreen = new Color(18, 92, 54);
    // 莊家區和玩家區的背景色。
    private final Color panelGreen = new Color(24, 120, 70);
    // 區塊邊框使用的金色。
    private final Color gold = new Color(230, 210, 150);

    // 剩餘牌數低於這個數字時，下一局開始前會重新洗牌。
    private static final int MIN_CARDS_FOR_NEW_ROUND = 12;
    // 莊家每次翻牌或補牌之間等待的毫秒數。
    private static final int DEALER_ACTION_DELAY_MS = 950;

    // 建構子：建立視窗、版面、按鈕，最後開始第一局。
    @SuppressWarnings("this-escape")
    public BlackJackUI() {
        // 設定視窗標題。
        setTitle("21點遊戲 Blackjack");
        // 設定視窗大小。
        setSize(950, 720);
        // 關閉視窗時結束程式。
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 讓視窗出現在螢幕中央。
        setLocationRelativeTo(null);
        // 整個視窗使用 BorderLayout，分成上中下區域。
        setLayout(new BorderLayout());
        // 設定視窗背景色。
        getContentPane().setBackground(tableGreen);

        // 建立最上方標題文字。
        JLabel titleLabel = new JLabel("21點遊戲 Blackjack", SwingConstants.CENTER);
        // 設定標題字型。
        titleLabel.setFont(titleFont);
        // 設定標題文字顏色。
        titleLabel.setForeground(Color.WHITE);
        // 設定標題周圍留白。
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 12, 0));
        // 把標題放到視窗最上方。
        add(titleLabel, BorderLayout.NORTH);

        // 建立中間區域，分成莊家區和玩家區兩列。
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 18));
        // 設定中間區域背景。
        centerPanel.setBackground(tableGreen);
        // 設定中間區域留白。
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 35, 10, 35));

        // 建立莊家區塊。
        JPanel dealerArea = createPlayArea("莊家 Dealer");
        // 建立莊家卡牌顯示區。
        dealerCardPanel = createCardPanel();
        // 建立莊家點數文字，一開始先隱藏。
        dealerScoreLabel = createInfoLabel("莊家點數：?");
        // 把莊家卡牌放到莊家區中間。
        dealerArea.add(dealerCardPanel, BorderLayout.CENTER);
        // 把莊家點數放到莊家區下方。
        dealerArea.add(dealerScoreLabel, BorderLayout.SOUTH);

        // 建立玩家區塊。
        JPanel playerArea = createPlayArea("玩家 Player");
        // 建立玩家卡牌顯示區。
        playerCardPanel = createCardPanel();
        // 建立玩家點數文字。
        playerScoreLabel = createInfoLabel("玩家點數：0");
        // 把玩家卡牌放到玩家區中間。
        playerArea.add(playerCardPanel, BorderLayout.CENTER);
        // 把玩家點數放到玩家區下方。
        playerArea.add(playerScoreLabel, BorderLayout.SOUTH);

        // 把莊家區加入中間面板。
        centerPanel.add(dealerArea);
        // 把玩家區加入中間面板。
        centerPanel.add(playerArea);
        // 把中間面板放到視窗中央。
        add(centerPanel, BorderLayout.CENTER);

        // 建立底部區域，放結果文字、按鈕和統計資料。
        JPanel bottomPanel = new JPanel(new BorderLayout());
        // 設定底部背景。
        bottomPanel.setBackground(tableGreen);
        // 設定底部留白。
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 20, 20));

        // 建立遊戲提示或結果文字。
        resultLabel = new JLabel("請選擇要牌或停牌", SwingConstants.CENTER);
        // 設定結果文字字型。
        resultLabel.setFont(new Font("PingFang TC", Font.BOLD, 22));
        // 設定結果文字顏色。
        resultLabel.setForeground(Color.WHITE);
        // 設定結果文字留白。
        resultLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 12, 0));
        // 把結果文字放到底部區域上方。
        bottomPanel.add(resultLabel, BorderLayout.NORTH);

        // 建立按鈕控制區。
        JPanel controlPanel = new JPanel();
        // 設定按鈕控制區背景。
        controlPanel.setBackground(tableGreen);

        // 建立要牌按鈕。
        hitButton = createButton("要牌 Hit");
        // 建立停牌按鈕。
        standButton = createButton("停牌 Stand");
        // 建立下一局按鈕。
        nextRoundButton = createButton("下一局 Continue");

        // 按下要牌按鈕時呼叫 hitCard。
        hitButton.addActionListener(e -> hitCard());
        // 按下停牌按鈕時呼叫 dealerTurn。
        standButton.addActionListener(e -> dealerTurn());
        // 按下下一局按鈕時呼叫 startRound。
        nextRoundButton.addActionListener(e -> startRound());

        // 把要牌按鈕加入控制區。
        controlPanel.add(hitButton);
        // 把停牌按鈕加入控制區。
        controlPanel.add(standButton);
        // 把下一局按鈕加入控制區。
        controlPanel.add(nextRoundButton);

        // 建立剩餘牌數文字。
        deckCountLabel = createInfoLabel("剩餘牌數：52");
        // 設定剩餘牌數文字顏色。
        deckCountLabel.setForeground(Color.WHITE);
        // 把剩餘牌數加入控制區。
        controlPanel.add(deckCountLabel);

        // 把控制區放到底部面板中央。
        bottomPanel.add(controlPanel, BorderLayout.CENTER);

        // 建立統計資料文字。
        statsLabel = createStatsLabel();
        // 把統計資料放到底部面板下方。
        bottomPanel.add(statsLabel, BorderLayout.SOUTH);

        // 把底部面板放到視窗下方。
        add(bottomPanel, BorderLayout.SOUTH);

        // 建立牌堆並開始第一局。
        startGame();
    }

    // 建立一個玩家區或莊家區的外框面板。
    private JPanel createPlayArea(String title) {
        // 使用 BorderLayout，讓標題、牌和點數可以上下排列。
        JPanel area = new JPanel(new BorderLayout());
        // 設定區塊背景色。
        area.setBackground(panelGreen);
        // 設定金色外框和內部留白。
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(gold, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // 建立區塊標題，例如「莊家 Dealer」或「玩家 Player」。
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        // 設定區塊標題字型。
        label.setFont(new Font("PingFang TC", Font.BOLD, 22));
        // 設定區塊標題顏色。
        label.setForeground(Color.WHITE);
        // 把標題放到區塊上方。
        area.add(label, BorderLayout.NORTH);

        // 回傳建立好的區塊。
        return area;
    }

    // 建立卡牌顯示區。
    private JPanel createCardPanel() {
        // FlowLayout 會讓卡牌從左到右排列，並保持置中。
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        // 設定卡牌區背景。
        panel.setBackground(panelGreen);
        // 回傳卡牌區。
        return panel;
    }

    // 建立一般資訊文字，例如點數、剩餘牌數。
    private JLabel createInfoLabel(String text) {
        // 建立置中的文字標籤。
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        // 套用主要字型。
        label.setFont(mainFont);
        // 設定文字顏色。
        label.setForeground(Color.WHITE);
        // 回傳文字標籤。
        return label;
    }

    // 建立遊戲控制按鈕。
    private JButton createButton(String text) {
        // 建立按鈕並設定顯示文字。
        JButton button = new JButton(text);
        // 套用主要字型。
        button.setFont(mainFont);
        // 關閉按鈕取得焦點時的虛線框。
        button.setFocusPainted(false);
        // 設定按鈕背景色。
        button.setBackground(new Color(245, 245, 245));
        // 設定按鈕文字顏色。
        button.setForeground(new Color(20, 20, 20));
        // 設定按鈕固定大小。
        button.setPreferredSize(new Dimension(165, 42));
        // 回傳按鈕。
        return button;
    }

    // 建立底部的對局統計文字。
    private JLabel createStatsLabel() {
        // 先建立空白資訊標籤。
        JLabel label = createInfoLabel("");
        // 統計文字比主要文字小一點。
        label.setFont(new Font("PingFang TC", Font.BOLD, 16));
        // 設定統計文字上方留白。
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        // 先把初始統計資料寫進 label。
        updateStatsLabel(label);
        // 回傳統計文字標籤。
        return label;
    }

    // 開始整個遊戲，通常只在程式剛啟動時呼叫。
    private void startGame() {
        // 清空牌堆。
        deck.clear();
        // 建立並洗好一副牌。
        createAndShuffleDeck();
        // 發第一局。
        startRound();
    }

    // 開始新的一局，但會沿用目前剩下的牌堆。
    private void startRound() {
        // 清空玩家手牌。
        playerCards.clear();
        // 清空莊家手牌。
        dealerCards.clear();
        // 新局開始，所以遊戲尚未結束。
        gameOver = false;
        // 新局開始，莊家還沒有行動。
        dealerTurnInProgress = false;
        // 新局開始，莊家的底牌還不能顯示。
        dealerCardsVisible = false;

        // 如果上一局的莊家動畫還在跑，先停止它。
        if (dealerTimer != null) {
            // 停止 Timer，避免上一局繼續補牌。
            dealerTimer.stop();
            // 清空 Timer 參考。
            dealerTimer = null;
        }

        // 記錄這局開始前是否重新洗牌。
        boolean reshuffled = false;
        // 如果剩餘牌太少，就重新洗一副牌。
        if (deck.size() < MIN_CARDS_FOR_NEW_ROUND) {
            // 重新建立並洗牌。
            createAndShuffleDeck();
            // 標記有重新洗牌，等等顯示提示文字。
            reshuffled = true;
        }

        // 發第一張牌給玩家。
        playerCards.add(drawCard());
        // 發第一張牌給莊家。
        dealerCards.add(drawCard());
        // 發第二張牌給玩家。
        playerCards.add(drawCard());
        // 發第二張牌給莊家，這張一開始會蓋牌。
        dealerCards.add(drawCard());

        // 新局開始，允許玩家要牌。
        hitButton.setEnabled(true);
        // 新局開始，允許玩家停牌。
        standButton.setEnabled(true);
        // 新局還沒結束，不能按下一局。
        nextRoundButton.setEnabled(false);

        // 如果剛剛有重新洗牌，就提示玩家。
        if (reshuffled) {
            // 顯示重新洗牌後的開局提示。
            resultLabel.setText("牌堆不足，已重新洗牌。請選擇要牌或停牌");
        } else {
            // 顯示一般開局提示。
            resultLabel.setText("請選擇要牌或停牌");
        }
        // 把提示文字顏色恢復成白色。
        resultLabel.setForeground(Color.WHITE);

        // 更新畫面，莊家第二張牌仍然蓋牌。
        updateDisplay(false);

        // 如果開局玩家直接拿到 21 點，直接進入莊家回合，避免玩家還能繼續要牌。
        if (calculateScore(playerCards) == 21) {
            dealerTurn();
        }
    }

    // 重新建立牌堆並洗牌。
    private void createAndShuffleDeck() {
        // 先清空目前牌堆。
        deck.clear();
        // 建立一副新牌，但排除目前桌面上已經出現的牌。
        createDeckExcludingCardsInPlay();
        // 打亂牌堆順序。
        Collections.shuffle(deck);
    }

    // 建立牌堆，並排除玩家與莊家目前手上的牌。
    private void createDeckExcludingCardsInPlay() {
        // 四種花色：S 黑桃、H 愛心、D 方塊、C 梅花。
        String[] suits = {"S", "H", "D", "C"};
        // 十三種牌面。
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        // 用文字記錄目前桌面上已經出現的牌。
        ArrayList<String> cardsInPlay = new ArrayList<>();

        // 把玩家手牌加入已出現清單。
        for (Card card : playerCards) {
            // imageName 會回傳像 AS、10H 這樣的牌名。
            cardsInPlay.add(card.imageName());
        }

        // 把莊家手牌加入已出現清單。
        for (Card card : dealerCards) {
            // 避免重新洗牌時抽到桌面上已經存在的牌。
            cardsInPlay.add(card.imageName());
        }

        // 逐一建立四種花色。
        for (String suit : suits) {
            // 逐一建立每個點數。
            for (String rank : ranks) {
                // 建立一張牌。
                Card card = new Card(rank, suit);

                // 如果這張牌還沒有在桌面上出現，才放進牌堆。
                if (!cardsInPlay.contains(card.imageName())) {
                    // 加入可抽的牌堆。
                    deck.add(card);
                }
            }
        }
    }

    // 從牌堆抽出最上面一張牌。
    private Card drawCard() {
        // 如果牌堆剛好空了，就重新洗牌。
        if (deck.isEmpty()) {
            // 重新建立牌堆，並排除目前桌面上的牌。
            createAndShuffleDeck();
        }

        // 移除並回傳牌堆第一張牌。
        return deck.remove(0);
    }

    // 玩家按下「要牌」時執行。
    private void hitCard() {
        // 如果遊戲已結束，或莊家正在行動，就不允許再要牌。
        if (gameOver || dealerTurnInProgress) {
            // 直接離開方法，不做任何事。
            return;
        }

        // 玩家抽一張牌。
        playerCards.add(drawCard());
        // 更新畫面，但莊家的底牌仍然不顯示。
        updateDisplay(false);

        // 計算玩家目前點數。
        int playerScore = calculateScore(playerCards);

        // 如果玩家點數超過 21，就是爆牌。
        if (playerScore > 21) {
            // 顯示玩家爆牌訊息。
            resultLabel.setText("你爆牌了，莊家獲勝！");
            // 輸的訊息用紅色。
            resultLabel.setForeground(new Color(255, 120, 120));
            // 記錄莊家獲勝，並且玩家有爆牌。
            recordDealerWin(true, false);
            // 結束這一局。
            endGame();
            // 結束後顯示莊家所有牌。
            updateDisplay(true);
        // 如果玩家剛好 21 點，就自動停牌換莊家。
        } else if (playerScore == 21) {
            // 進入莊家回合。
            dealerTurn();
        }
    }

    // 玩家按下「停牌」或玩家 21 點時，進入莊家回合。
    private void dealerTurn() {
        // 如果遊戲已結束，或莊家已經在行動，就不要重複開始。
        if (gameOver || dealerTurnInProgress) {
            // 直接離開方法。
            return;
        }

        // 標記莊家回合正在進行。
        dealerTurnInProgress = true;
        // 莊家回合剛開始時，底牌還沒翻開。
        dealerCardsVisible = false;
        // 莊家行動時，玩家不能要牌。
        hitButton.setEnabled(false);
        // 莊家行動時，玩家不能再按停牌。
        standButton.setEnabled(false);
        // 莊家行動時，還不能進入下一局。
        nextRoundButton.setEnabled(false);

        // 顯示莊家即將翻牌的提示。
        resultLabel.setText("莊家準備翻牌...");
        // 文字顏色維持白色。
        resultLabel.setForeground(Color.WHITE);

        // 建立 Timer，讓莊家每隔一段時間才執行 advanceDealerTurn。
        dealerTimer = new Timer(DEALER_ACTION_DELAY_MS, e -> advanceDealerTurn());
        // 設定第一次觸發前也等待同樣的時間。
        dealerTimer.setInitialDelay(DEALER_ACTION_DELAY_MS);
        // 啟動莊家動畫流程。
        dealerTimer.start();
    }

    // Timer 每次觸發時，推進莊家回合的一小步。
    private void advanceDealerTurn() {
        // 如果莊家的底牌還沒有翻開，第一步先翻開底牌。
        if (!dealerCardsVisible) {
            // 標記莊家牌已經可以全部顯示。
            dealerCardsVisible = true;
            // 顯示翻開底牌訊息。
            resultLabel.setText("莊家翻開底牌...");
            // 更新畫面並顯示莊家所有牌。
            updateDisplay(true);
            // 這次 Timer 只做翻牌，下一次再判斷要不要補牌。
            return;
        }

        // 莊家點數小於 17 時必須繼續補牌。
        if (calculateScore(dealerCards) < 17) {
            // 莊家抽一張牌。
            dealerCards.add(drawCard());
            // 顯示莊家補牌訊息。
            resultLabel.setText("莊家補牌...");
            // 更新畫面並顯示新牌。
            updateDisplay(true);
            // 這次 Timer 只補一張，下一次再重新判斷點數。
            return;
        }

        // 莊家點數已經 17 以上，不需要再補牌。
        dealerTimer.stop();
        // 清空 Timer 參考。
        dealerTimer = null;
        // 比較雙方點數並結算。
        finishDealerTurn();
    }

    // 莊家停止補牌後，判斷本局勝負。
    private void finishDealerTurn() {
        // 計算玩家最終點數。
        int playerScore = calculateScore(playerCards);
        // 計算莊家最終點數。
        int dealerScore = calculateScore(dealerCards);

        // 顯示莊家全部牌與最終點數。
        updateDisplay(true);

        // 如果莊家超過 21 點，就是莊家爆牌，玩家贏。
        if (dealerScore > 21) {
            // 顯示玩家獲勝訊息。
            resultLabel.setText("莊家爆牌，你贏了！");
            // 贏的訊息用綠色。
            resultLabel.setForeground(new Color(120, 255, 150));
            // 記錄玩家獲勝，並且莊家有爆牌。
            recordPlayerWin(false, true);
        // 如果玩家點數比莊家大，玩家贏。
        } else if (playerScore > dealerScore) {
            // 顯示玩家獲勝訊息。
            resultLabel.setText("你贏了！");
            // 贏的訊息用綠色。
            resultLabel.setForeground(new Color(120, 255, 150));
            // 記錄玩家獲勝，雙方都沒有爆牌。
            recordPlayerWin(false, false);
        // 如果玩家點數比莊家小，玩家輸。
        } else if (playerScore < dealerScore) {
            // 顯示玩家輸了。
            resultLabel.setText("你輸了！");
            // 輸的訊息用紅色。
            resultLabel.setForeground(new Color(255, 120, 120));
            // 記錄莊家獲勝，雙方都沒有爆牌。
            recordDealerWin(false, false);
        // 其他情況就是雙方點數一樣，平手。
        } else {
            // 顯示平手訊息。
            resultLabel.setText("平手！");
            // 平手訊息用黃色。
            resultLabel.setForeground(new Color(255, 220, 90));
            // 記錄平手。
            recordTie();
        }

        // 莊家回合結束。
        dealerTurnInProgress = false;
        // 結束這一局並開放下一局按鈕。
        endGame();
    }

    // 記錄玩家獲勝。
    private void recordPlayerWin(boolean playerBusted, boolean dealerBusted) {
        // 總局數加一。
        totalRounds++;
        // 玩家勝場加一。
        playerWins++;

        // 如果玩家爆牌，就記錄玩家爆牌次數。
        if (playerBusted) {
            // 玩家爆牌次數加一。
            playerBusts++;
        }

        // 如果莊家爆牌，就記錄莊家爆牌次數。
        if (dealerBusted) {
            // 莊家爆牌次數加一。
            dealerBusts++;
        }

        // 更新底部統計文字。
        updateStatsLabel();
    }

    // 記錄莊家獲勝，也就是玩家輸。
    private void recordDealerWin(boolean playerBusted, boolean dealerBusted) {
        // 總局數加一。
        totalRounds++;
        // 莊家勝場加一。
        dealerWins++;

        // 如果玩家爆牌，就記錄玩家爆牌次數。
        if (playerBusted) {
            // 玩家爆牌次數加一。
            playerBusts++;
        }

        // 如果莊家爆牌，就記錄莊家爆牌次數。
        if (dealerBusted) {
            // 莊家爆牌次數加一。
            dealerBusts++;
        }

        // 更新底部統計文字。
        updateStatsLabel();
    }

    // 記錄平手。
    private void recordTie() {
        // 總局數加一。
        totalRounds++;
        // 平手次數加一。
        ties++;
        // 更新底部統計文字。
        updateStatsLabel();
    }

    // 更新主畫面上的統計文字。
    private void updateStatsLabel() {
        // 使用 statsLabel 欄位作為要更新的目標。
        updateStatsLabel(statsLabel);
    }

    // 更新指定 label 的統計文字，建立 statsLabel 時也會用到。
    private void updateStatsLabel(JLabel label) {
        // 如果還沒有任何對局，勝率顯示 0，避免除以 0。
        double winRate = totalRounds == 0 ? 0 : (playerWins * 100.0 / totalRounds);
        // 用格式化字串產生統計文字。
        label.setText(String.format(
                "對局數：%d | 勝：%d | 敗：%d | 平：%d | 勝率：%.1f%% | 玩家爆牌：%d | 莊家爆牌：%d",
                totalRounds,
                playerWins,
                dealerWins,
                ties,
                winRate,
                playerBusts,
                dealerBusts
        ));
    }

    // 結束目前這一局。
    private void endGame() {
        // 標記遊戲結束，避免玩家繼續要牌或停牌。
        gameOver = true;
        // 關閉要牌按鈕。
        hitButton.setEnabled(false);
        // 關閉停牌按鈕。
        standButton.setEnabled(false);
        // 開啟下一局按鈕。
        nextRoundButton.setEnabled(true);
    }

    // 計算一組牌的 21 點分數。
    private int calculateScore(List<Card> cards) {
        // 一般分數總和。
        int score = 0;
        // A 的數量，用來處理 A 可以是 11 或 1。
        int aceCount = 0;

        // 逐張牌計算點數。
        for (Card card : cards) {
            // A 先當 11 點計算。
            if (card.rank.equals("A")) {
                // A 加 11 點。
                score += 11;
                // 記錄有一張 A。
                aceCount++;
            // J、Q、K 都算 10 點。
            } else if (card.rank.equals("J") || card.rank.equals("Q") || card.rank.equals("K")) {
                // 人頭牌加 10 點。
                score += 10;
            // 其他數字牌直接用牌面數字。
            } else {
                // 把字串轉成整數後加到分數。
                score += Integer.parseInt(card.rank);
            }
        }

        // 如果爆牌且手上有 A，就把 A 從 11 點改成 1 點。
        while (score > 21 && aceCount > 0) {
            // 11 點改成 1 點，相當於總分減 10。
            score -= 10;
            // 已經調整過一張 A。
            aceCount--;
        }

        // 回傳最後計算出的點數。
        return score;
    }

    // 更新畫面上的莊家牌、玩家牌、點數和剩餘牌數。
    private void updateDisplay(boolean showDealerAllCards) {
        // 清空莊家卡牌區，準備重新放入最新卡牌。
        dealerCardPanel.removeAll();
        // 清空玩家卡牌區，準備重新放入最新卡牌。
        playerCardPanel.removeAll();

        // 逐張顯示莊家的牌。
        for (int i = 0; i < dealerCards.size(); i++) {
            // 莊家第二張牌是底牌；如果還不能顯示，就用牌背。
            if (i == 1 && !showDealerAllCards) {
                // 顯示牌背圖片。
                dealerCardPanel.add(createCardImageLabel("BACK"));
            // 其他牌，或莊家已翻牌時，就顯示正面。
            } else {
                // 顯示莊家這張牌的圖片。
                dealerCardPanel.add(createCardImageLabel(dealerCards.get(i).imageName()));
            }
        }

        // 逐張顯示玩家手牌。
        for (Card card : playerCards) {
            // 玩家所有牌都正面顯示。
            playerCardPanel.add(createCardImageLabel(card.imageName()));
        }

        // 如果莊家所有牌都可以顯示，就顯示莊家實際點數。
        if (showDealerAllCards) {
            // 顯示莊家點數。
            dealerScoreLabel.setText("莊家點數：" + calculateScore(dealerCards));
        // 如果莊家底牌還沒翻開，就隱藏莊家點數。
        } else {
            // 用問號隱藏莊家點數。
            dealerScoreLabel.setText("莊家點數：?");
        }

        // 顯示玩家目前點數。
        playerScoreLabel.setText("玩家點數：" + calculateScore(playerCards));
        // 顯示牌堆剩餘牌數。
        deckCountLabel.setText("剩餘牌數：" + deck.size());

        // 通知 Swing 重新計算莊家卡牌區版面。
        dealerCardPanel.revalidate();
        // 通知 Swing 重畫莊家卡牌區。
        dealerCardPanel.repaint();

        // 通知 Swing 重新計算玩家卡牌區版面。
        playerCardPanel.revalidate();
        // 通知 Swing 重畫玩家卡牌區。
        playerCardPanel.repaint();
    }

    // 根據牌名建立卡牌圖片 label。
    private JLabel createCardImageLabel(String imageName) {
        // 圖片路徑，例如 cards/AS.png。
        String path = "cards/" + imageName + ".png";
        // 用圖片路徑建立 ImageIcon。
        ImageIcon icon = new ImageIcon(path);

        // 如果圖片載入失敗，icon 寬度會小於等於 0。
        if (icon.getIconWidth() <= 0) {
            // 圖片不存在時，改用文字卡牌作為備援。
            return createTextCardLabel(imageName);
        }

        // 把圖片縮放成固定卡牌大小。
        Image image = icon.getImage().getScaledInstance(90, 135, Image.SCALE_SMOOTH);
        // 建立顯示圖片的 label。
        JLabel label = new JLabel(new ImageIcon(image));
        // 固定卡牌 label 大小，避免版面跳動。
        label.setPreferredSize(new Dimension(90, 135));
        // 回傳圖片卡牌。
        return label;
    }

    // 當圖片不存在時，用文字畫出一張簡易卡牌。
    private JLabel createTextCardLabel(String imageName) {
        // 建立置中的文字 label。
        JLabel label = new JLabel(imageName, SwingConstants.CENTER);
        // 設定卡牌固定大小。
        label.setPreferredSize(new Dimension(90, 135));
        // 開啟不透明背景，才看得到背景色。
        label.setOpaque(true);

        // 如果要顯示牌背。
        if (imageName.equals("BACK")) {
            // 牌背顯示中文文字。
            label.setText("牌背");
            // 牌背背景使用深藍色。
            label.setBackground(new Color(20, 50, 100));
            // 牌背文字使用白色。
            label.setForeground(Color.WHITE);
        // 否則就是正面的文字卡牌。
        } else {
            // 正面卡牌背景使用白色。
            label.setBackground(Color.WHITE);

            // 愛心和方塊是紅色花色。
            if (imageName.endsWith("H") || imageName.endsWith("D")) {
                // 紅色花色用紅字。
                label.setForeground(Color.RED);
            // 黑桃和梅花是黑色花色。
            } else {
                // 黑色花色用黑字。
                label.setForeground(Color.BLACK);
            }
        }

        // 設定卡牌黑色邊框。
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        // 設定卡牌文字字型。
        label.setFont(new Font("PingFang TC", Font.BOLD, 20));
        // 回傳文字卡牌。
        return label;
    }

    // Card 代表一張撲克牌。
    private static class Card {
        // 牌面，例如 A、2、10、J、Q、K。
        final String rank;
        // 花色，例如 S、H、D、C。
        final String suit;

        // 建立一張牌時，要傳入牌面和花色。
        Card(String rank, String suit) {
            // 儲存牌面。
            this.rank = rank;
            // 儲存花色。
            this.suit = suit;
        }

        // 回傳圖片檔名使用的牌名，例如 AS、10H。
        String imageName() {
            // 圖片名稱就是牌面加花色。
            return rank + suit;
        }
    }

    // Java 程式入口。
    public static void main(String[] args) {
        // Swing 介面要在 Event Dispatch Thread 裡建立。
        SwingUtilities.invokeLater(() -> {
            // 建立遊戲視窗。
            BlackJackUI game = new BlackJackUI();
            // 顯示遊戲視窗。
            game.setVisible(true);
        });
    }
}
