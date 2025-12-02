package entity;

import entity.Entity.Team;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a pool of recyclable weapons.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class WeaponPool {
    
    /**
     * Set of already created weapons.
     */
    private static Set<Weapon> pool = new HashSet<Weapon>();
    
    /**
     * Constructor, not called.
     */
    private WeaponPool() {
    
    }
    
    /**
     * Returns a weapon from the pool if one is available, a new one if there isn't.
     *
     * @param positionX Requested position of the weapon in the X axis.
     * @param positionY Requested position of the weapon in the Y axis.
     * @param speed     Requested speed of the weapon, positive or negative depending on direction -
     * positive is down.
     * @param width     Requested size of the weapon width.
     * @param height    Requested size of the weapon height.
     * @param team      Requested team type.
     * @return Requested weapon.
     */
    public static Weapon getWeapon(final int positionX,
        final int positionY, final int speed, final int width, final int height, final Team team) {
        Weapon weapon;
        if (!pool.isEmpty()) {
            weapon = pool.iterator().next();
            pool.remove(weapon);
            weapon.setPositionX(positionX - width / 2);
            weapon.setPositionY(positionY);
            weapon.setSpeed(speed);
            weapon.setSize(width, height);  // weapon size
            weapon.setTeam(team);    // team setting
            
            // User's logic applied to recycled weapon
            weapon.setSpeedX(0);
            weapon.setBossBullet(false);
            weapon.setRotation(0);
            weapon.resetHoming();
        } else {
            weapon = new Weapon(positionX, positionY, width, height, speed);
            weapon.setPositionX(positionX - width / 2);
            weapon.setSize(width, height); // weapon size
            weapon.setTeam(team); // team setting
            
            // User's logic applied to new weapon
            weapon.setSpeedX(0);
            weapon.setBossBullet(false);
            weapon.setRotation(0);
            weapon.resetHoming();
        }
        weapon.setSpriteMap();
        return weapon;
    }
    
    /**
     * Returns a weapon from the pool if one is available, a new one if there isn't.
     *
     * @param positionX Requested position of the weapon in the X axis.
     * @param positionY Requested position of the weapon in the Y axis.
     * @param speed     Requested speed of the weapon, positive or negative depending on direction -
     * positive is down.
     * @param width     Requested size of the weapon width.
     * @param height    Requested size of the weapon height.
     * @param team      Requested team type.
     * @return Requested weapon.
     */
    public static Weapon getWeapon(final int positionX,
        final int positionY, final int width, final int height, final int speed, final int damage,
        final Team team) {
        Weapon weapon;
        if (!pool.isEmpty()) {
            weapon = pool.iterator().next();
            pool.remove(weapon);
            weapon.setPositionX(positionX - width / 2);
            weapon.setPositionY(positionY);
            weapon.setSpeed(speed);
            weapon.setSize(width, height);  // weapon size
            weapon.setTeam(team);    // team setting
            weapon.setDamage(damage);
            
            // User's logic applied to recycled weapon (consistency)
            weapon.setSpeedX(0);
            weapon.setBossBullet(false);
            weapon.setRotation(0);
            weapon.resetHoming();
        } else {
            weapon = new Weapon(positionX - width / 2, positionY, width, height, speed, damage);
            weapon.setTeam(team); // team setting
            
            // User's logic applied to new weapon (consistency)
            weapon.setSpeedX(0);
            weapon.setBossBullet(false);
            weapon.setRotation(0);
            weapon.resetHoming();
        }
        weapon.setSpriteMap();
        return weapon;
    }
    
    /**
     * Adds one or more weapons to the list of available ones.
     *
     * @param weapon weapons to recycle.
     */
    public static void recycle(final Set<Weapon> weapon) {
        pool.addAll(weapon);
    }
}