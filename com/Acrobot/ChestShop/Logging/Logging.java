package com.Acrobot.ChestShop.Logging;

import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.DB.Queue;
import com.Acrobot.ChestShop.DB.Transaction;
import com.Acrobot.ChestShop.Items.Items;
import com.Acrobot.ChestShop.Shop.Shop;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Acrobot
 */
public class Logging {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final Logger logger = Logger.getLogger("ChestShop");

    private static String getDateAndTime() {
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void log(String string) {
        if (Config.getBoolean(Property.LOG_TO_CONSOLE)) logger.log(Level.INFO, "[ChestShop] " + string);
        if (Config.getBoolean(Property.LOG_TO_FILE)) FileWriterQueue.addToQueue(getDateAndTime() + ' ' + string);
    }

    public static void logTransaction(boolean isBuying, Shop shop, double price, Player player) {
        log(player.getName()
                + (isBuying ? " bought " : " sold ")
                + shop.stockAmount + ' '
                + Items.getSignName(shop.stock) + " for "
                + price + (isBuying ? " from " : " to ")
                + shop.owner + " at "
                + locationToString(shop.sign.getLocation()));
        if (Config.getBoolean(Property.LOG_TO_DATABASE) || Config.getBoolean(Property.GENERATE_STATISTICS_PAGE)) logToDatabase(isBuying, shop, price, player);
    }

    private static String locationToString(Location loc) {
        return '[' + loc.getWorld().getName() + "] " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private static void logToDatabase(boolean isBuying, Shop shop, double price, Player player) {
        Transaction transaction = new Transaction();

        transaction.setAmount(shop.stockAmount);
        transaction.setBuy(isBuying);

        ItemStack stock = shop.stock;

        transaction.setItemDurability(stock.getDurability());
        transaction.setItemID(stock.getTypeId());
        transaction.setPrice((float) price);
        transaction.setSec(System.currentTimeMillis() / 1000);
        transaction.setShopOwner(shop.owner);
        transaction.setShopUser(player.getName());

        Queue.addToQueue(transaction);
    }
}
