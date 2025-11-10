package entity;

import java.awt.Color;
import java.util.Random;

import engine.utils.Cooldown;
import engine.Core;
import engine.AssetManager.SpriteType;
import engine.GameSettings;

/**
 * EnemyShip을 확장하는 특수화된 적함인 보스함(BossShip)을 구현합니다.
 * 초기에는 보스에 특정한 속성으로 설정됩니다.
 *
 */
public class BossShip extends EnemyShip {

    /** 보스 적의 기본 너비 */
    private static final int BOSS_DEFAULT_WIDTH = 21 * 2;
    /** 보스 적의 기본 높이 */
    private static final int BOSS_DEFAULT_HEIGHT = 10 * 2;

    // 매우 높은 값으로 초기 HP, 점수, 코인 값을 정의합니다.
    /** 보스 적의 점수 값. */
    private static final int BOSS_TYPE_POINTS = 50000;
    /** 보스 적의 코인 값. */
    private static final int BOSS_TYPE_COINS = 20000;
    /** 보스 적의 초기 체력. */
    private static final int BOSS_INITIAL_HEALTH = 2000;

    // 보스 전용 쿨다운이나 속성을 여기에 선언할 수 있습니다.
    // private Cooldown specialAttackCooldown;

    /**
     * 보스함의 속성을 설정하는 생성자입니다.
     * EnemyShip의 생성자를 호출하여 기본 속성을 설정하지만, 보스에 특화된 크기, 점수, 코인, 체력을 설정합니다.
     *
     * @param positionX
     * 함선의 초기 X축 위치.
     * @param positionY
     * 함선의 초기 Y축 위치.
     * @param spriteType
     * 함선에 해당하는 스프라이트 타입 (예: BossEnemy1).
     */
    public BossShip(final int positionX, final int positionY,
                    final SpriteType spriteType) {
        // EnemyShip의 주 생성자를 호출합니다. 이 생성자에서 기본 보스 스탯(1000점, 50체력)이 설정됩니다.
        super(positionX, positionY, spriteType);

        // EnemyShip의 생성자에서 설정된 기본 크기(12*2, 8*2)를 보스의 올바른 크기로 재정의합니다.
        this.width = BOSS_DEFAULT_WIDTH;
        this.height = BOSS_DEFAULT_HEIGHT;
        // 보스에게 눈에 띄는 색상을 지정합니다. (선택 사항)
        this.color = Color.MAGENTA;

        // 부모 클래스(EnemyShip)에서 설정된 기본 스탯을 매우 높은 값으로 덮어씁니다.
        // 이 필드들이 package-private 접근 레벨을 가지고 있다고 가정합니다.
        this.pointValue = BOSS_TYPE_POINTS;
        this.coinValue = BOSS_TYPE_COINS;
        this.health = BOSS_INITIAL_HEALTH;

        // 체력 비율 계산 및 데미지 색상 변화 로직을 위해 초기 체력 값도 덮어씁니다.
        // NOTE: initialHealth는 EnemyShip에서 private으로 선언되어 있지만,
        // 프로젝트 구조상 동일 패키지에서 접근 가능하다고 가정하고 설정합니다.
        // this.initialHealth = BOSS_INITIAL_HEALTH;

        // 보스 전용 로직 초기화:
        // this.specialAttackCooldown = Core.getCooldown(5000);
        // this.specialAttackCooldown.reset();
    }

    /**
     * 보스함의 속성을 업데이트합니다. (예: 애니메이션, 특수 공격 타이밍)
     */
    // @Override // EnemyShip의 update()를 재정의합니다.
    // public final void update() {
    //     super.update();
    //
    //     // 여기에 보스 특유의 업데이트 로직을 추가합니다.
    // }

    // 추가적인 보스함 전용 메서드(예: 특수 공격 발사)는 여기에 추가할 수 있습니다.
    // public void fireSpecialAttack(Set<Bullet> bullets) {}
}