package engine.renderer;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * 프로젝트에 포함된 모든 그래픽 리소스(SpriteType)를 시각적으로 확인하기 위한 테스트 클래스입니다. 픽셀 아트(비트맵)와 일반 이미지(BufferedImage),
 * 그리고 움직이는 애니메이션을 모두 지원합니다.
 */
public class AssetVisualizerTest extends JFrame {
    
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SPRITE_COLOR = Color.GREEN; // 픽셀 아트 기본 색상
    
    private static final int GRID_COLUMNS = 6;
    private static final int GRID_GAP = 20;
    
    // [설정] 애니메이션 프레임 변경 속도 (ms 단위, 낮을수록 빠름)
    private static final int ANIMATION_DELAY = 150;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AssetVisualizerTest viewer = new AssetVisualizerTest();
            viewer.setVisible(true);
        });
    }
    
    public AssetVisualizerTest() {
        setTitle("Invaders Pro 9 - Asset Visualizer (Animation Supported)");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 메인 컨테이너 설정
        JPanel mainPanel = new JPanel(new GridLayout(0, GRID_COLUMNS, GRID_GAP, GRID_GAP));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 스크롤 패널 추가
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        // 모든 SpriteType에 대해 패널 생성
        loadAndAddSprites(mainPanel);
        
        // 화면을 주기적으로 갱신하여 애니메이션 효과를 주는 타이머 시작
        startAnimationLoop();
    }
    
    private void startAnimationLoop() {
        // 지정된 시간(ANIMATION_DELAY)마다 repaint()를 호출하여 화면을 다시 그립니다.
        // 이것이 없으면 화면이 정지된 상태로 보입니다.
        Timer timer = new Timer(ANIMATION_DELAY, e -> repaint());
        timer.start();
    }
    
    private void loadAndAddSprites(JPanel container) {
        AssetManager assetManager = AssetManager.getInstance();
        
        // SpriteType 열거형의 모든 값을 가져와서 순회
        Arrays.stream(SpriteType.values()).forEach(type -> {
            JPanel spritePanel = new JPanel(new BorderLayout());
            spritePanel.setPreferredSize(new Dimension(150, 150));
            spritePanel.setBackground(new Color(30, 30, 30)); // 개별 카드 배경색
            spritePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            
            // 1. 그래픽 렌더링 패널 (커스텀 그리기)
            JPanel canvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // 패널 크기를 넘겨주어 중앙 정렬 계산
                    drawSprite(g, assetManager, type, getWidth(), getHeight());
                }
            };
            canvas.setOpaque(false); // 투명하게 해서 배경색 보이게 설정
            
            // 2. 이름 라벨
            JLabel nameLabel = new JLabel(type.name(), SwingConstants.CENTER);
            nameLabel.setForeground(TEXT_COLOR);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            spritePanel.add(canvas, BorderLayout.CENTER);
            spritePanel.add(nameLabel, BorderLayout.SOUTH);
            
            container.add(spritePanel);
        });
    }
    
    /**
     * 실제 스프라이트나 이미지를 그리는 로직. 움직이는 애니메이션(프레임 > 1)인 경우, 시간에 따라 이미지를 교체하여 그립니다.
     */
    private void drawSprite(Graphics g, AssetManager assetManager, SpriteType type, int pWidth,
        int pHeight) {
        Graphics2D g2d = (Graphics2D) g;
        
        // 이미지 타입인 경우 (캐릭터, 아이템 등)
        if (type.isImage()) {
            BufferedImage image = null;
            
            // [애니메이션 처리] 프레임이 여러 개인 경우
            if (type.getFrameCount() > 1) {
                // AssetManager에서 애니메이션 배열을 가져옴
                BufferedImage[] frames = assetManager.getAnimation(type);
                
                if (frames != null && frames.length > 0) {
                    // 현재 시간을 기준으로 보여줄 프레임 인덱스 계산
                    // 공식: (현재시간 / 딜레이) % 전체 프레임 수
                    long frameIndex =
                        (System.currentTimeMillis() / ANIMATION_DELAY) % frames.length;
                    image = frames[(int) frameIndex];
                } else {
                    // 애니메이션 배열 로드 실패 시, 기본 이미지 시도 (안전 장치)
                    image = assetManager.getSpriteImage(type);
                }
            } else {
                // 단일 이미지인 경우
                image = assetManager.getSpriteImage(type);
            }
            
            // 이미지 그리기
            if (image != null) {
                // 패널 중앙에 위치하도록 좌표 계산
                int x = (pWidth - image.getWidth()) / 2;
                int y = (pHeight - image.getHeight()) / 2;
                g2d.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
            } else {
                drawError(g2d, pWidth, pHeight);
            }
        } else { // 픽셀 아트(비트맵) 타입인 경우 (기존 적 우주선 등)
            boolean[][] spriteMap = assetManager.getSpriteMap(type);
            if (spriteMap != null) {
                int spriteWidth = spriteMap.length;
                int spriteHeight = spriteMap[0].length;
                
                // 픽셀 아트는 작으므로 2배 확대
                int scale = 2;
                
                int drawWidth = spriteWidth * scale;
                int drawHeight = spriteHeight * scale;
                int startX = (pWidth - drawWidth) / 2;
                int startY = (pHeight - drawHeight) / 2;
                
                g2d.setColor(SPRITE_COLOR);
                for (int i = 0; i < spriteWidth; i++) {
                    for (int j = 0; j < spriteHeight; j++) {
                        if (spriteMap[i][j]) {
                            g2d.fillRect(startX + (i * scale), startY + (j * scale), scale, scale);
                        }
                    }
                }
            } else {
                drawError(g2d, pWidth, pHeight);
            }
        }
    }
    
    private void drawError(Graphics2D g2d, int w, int h) {
        g2d.setColor(Color.RED);
        g2d.drawString("No Data", w / 2 - 20, h / 2 + 5);
    }
}