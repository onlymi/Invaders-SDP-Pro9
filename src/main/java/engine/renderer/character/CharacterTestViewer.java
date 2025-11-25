package engine.renderer.character;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import engine.Core;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class CharacterTestViewer extends JPanel implements ActionListener {
    
    // 1. 캐릭터 정보 정의 (이름, 색상, 기본 스프라이트, 포즈 스프라이트)
    private static class CharacterInfo {
        
        String name;
        Color color;
        SpriteType walk1;
        SpriteType walk2;
        
        public CharacterInfo(String name, Color color, SpriteType walk1,
            SpriteType walk2) {
            this.name = name;
            this.color = color;
            this.walk1 = walk1;
            this.walk2 = walk2;
        }
    }
    
    // 7가지 캐릭터 정의
    private final CharacterInfo[] characters = {
        new CharacterInfo("Warrior", new Color(255, 80, 80),
            SpriteType.CharacterWarriorWalk1, SpriteType.CharacterWarriorWalk2),
        new CharacterInfo("Archer", new Color(80, 255, 80),
            SpriteType.CharacterArcherWalk1, SpriteType.CharacterArcherWalk2),
        new CharacterInfo("Wizard", new Color(80, 150, 255),
            SpriteType.CharacterWizardWalk1, SpriteType.CharacterWizardWalk2),
        new CharacterInfo("Laser", Color.CYAN,
            SpriteType.CharacterLaserWalk1, SpriteType.CharacterLaserWalk2),
        new CharacterInfo("Electric", Color.YELLOW,
            SpriteType.CharacterElectricWalk1, SpriteType.CharacterElectricWalk2),
        new CharacterInfo("Bomber", Color.ORANGE,
            SpriteType.CharacterBomberWalk1, SpriteType.CharacterBomberWalk2),
        new CharacterInfo("Healer", Color.WHITE, SpriteType.CharacterHealerWalk1,
            SpriteType.CharacterHealerWalk2),
    };
    
    // Setting viewer
    private Timer timer;
    private int currentFrame = 1; // 0: Basic, 1: Walk1, 2: Walk2
    private final int SCALE = 2;  // 확대 배율
    private final int GAP = 120;  // 캐릭터 간 간격
    
    public CharacterTestViewer() {
        AssetManager.getInstance();
        
        // 0.5초(500ms)마다 애니메이션 프레임 변경
        this.timer = new Timer(500, this);
        this.timer.start();
        
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(900, 300));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int startX = 50;
        int startY = 100;
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        
        for (int i = 0; i < this.characters.length; i++) {
            CharacterInfo info = this.characters[i];
            int x = startX + (i * this.GAP);
            
            SpriteType currentSpriteType = null;
            
            if (this.currentFrame == 1) {
                currentSpriteType = info.walk1;
            } else if (this.currentFrame == 2) {
                currentSpriteType = info.walk2;
            }
            
            boolean[][] spriteData = Core.getAssetManager().getSpriteMap(currentSpriteType);
            
            if (spriteData != null) {
                drawCharacter(g, spriteData, x, startY, this.SCALE, info.color);
            } else {
                g.setColor(Color.RED);
                g.drawString("No Data", x, startY);
            }
            
            g.setColor(Color.WHITE);
            g.drawString(info.name, x, startY + 150);
        }
    }
    
    /**
     * 실제 스프라이트를 그리는 메서드 AssetManager의 boolean[][]은 [width][height] (x, y) 순서로 저장되어 있습니다.
     */
    private void drawCharacter(Graphics g, boolean[][] sprite, int x, int y, int scale, Color c) {
        g.setColor(c);
        
        int width = sprite.length;
        int height = sprite[0].length;
        
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                if (sprite[col][row]) {
                    g.fillRect(x + (row * scale), y + (col * scale), scale, scale);
                }
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.currentFrame == 1) {
            this.currentFrame = 2;
        } else {
            this.currentFrame = 1;
        }
        repaint();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("AssetManager Character Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        CharacterTestViewer viewer = new CharacterTestViewer();
        frame.add(viewer);
        
        frame.pack();
        frame.setLocationRelativeTo(null); // 화면 중앙 배치
        frame.setVisible(true);
    }
}