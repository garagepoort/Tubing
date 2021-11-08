package be.garagepoort.mcioc.gui.model;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TubingGuiItemStack {

    private Material material;
    private TubingGuiItemText name;
    private boolean enchanted;
    private List<ItemStackLoreLine> loreLines;

    public TubingGuiItemStack(Material material, TubingGuiItemText name, boolean enchanted, List<ItemStackLoreLine> loreLines) {
        this.material = material;
        this.name = name;
        this.enchanted = enchanted;
        this.loreLines = loreLines;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<ItemStackLoreLine> getLoreLines() {
        return loreLines;
    }

    public void setLoreLines(List<ItemStackLoreLine> loreLines) {
        this.loreLines = loreLines;
    }

    public void setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(1);

        addName(itemStack, name.toString());
        addLore(itemStack, loreLines);

        ItemMeta meta = itemStack.getItemMeta();
        if (enchanted) {
            itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void addName(ItemStack itemStack, String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name.equals("") ? " " : format(name));
        itemStack.setItemMeta(itemMeta);
    }

    private void addLore(ItemStack itemStack, List<ItemStackLoreLine> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> original = itemMeta.getLore();
        if (original == null) original = new ArrayList<>();
        for (ItemStackLoreLine itemStackLoreLine : lore) {
            String formattedLoreLine = itemStackLoreLine.getParts().stream()
                    .map(l -> this.format(l.toString()))
                    .collect(Collectors.joining());
            original.add(formattedLoreLine);
        }
        itemMeta.setLore(original);
        itemStack.setItemMeta(itemMeta);
    }

    private String format(String loreLine) {
        return ChatColor.translateAlternateColorCodes('&', loreLine);
    }
}
