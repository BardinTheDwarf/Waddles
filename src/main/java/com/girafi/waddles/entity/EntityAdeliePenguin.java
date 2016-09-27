package com.girafi.waddles.entity;

import com.girafi.waddles.Waddles;
import com.girafi.waddles.utils.ConfigurationHandler;
import com.girafi.waddles.utils.WaddlesSounds;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood.FishType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;

public class EntityAdeliePenguin extends EntityAnimal {
    private static final Set<Item> TEMPTATION_ITEMS = Sets.newHashSet(new ItemStack(Items.FISH, 1, FishType.COD.getMetadata()).getItem(), new ItemStack(Items.FISH, 1, FishType.SALMON.getMetadata()).getItem());
    public short rotationFlipper;
    public boolean moveFlipper = false;

    public EntityAdeliePenguin(World world) {
        super(world);
        this.setSize(0.4F, 0.95F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIExtinguishFire());
        this.tasks.addTask(2, new EntityAIPanic(this, 1.5D));
        this.tasks.addTask(3, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, false, TEMPTATION_ITEMS));
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityAdeliePenguin.class, 6.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isChild() ? WaddlesSounds.ADELIE_BABY_AMBIENT : WaddlesSounds.ADELIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound() {
        return WaddlesSounds.ADELIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WaddlesSounds.ADELIE_DEATH;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (worldObj.isRemote) {
            if (this.posZ != this.prevPosZ) {
                if (moveFlipper) {
                    rotationFlipper++;
                }
            }
        }
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        if (ConfigurationHandler.dropExp) {
            return super.getExperiencePoints(player);
        }
        return 0;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isBreedingItem(@Nullable ItemStack stack) {
        return stack != null && TEMPTATION_ITEMS.contains(stack.getItem());
    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() {
        if (ConfigurationHandler.dropFish) {
            return Waddles.LOOT_ENTITIES_PENGUIN_FISH;
        }
        return null;
    }

    @Override
    public EntityAdeliePenguin createChild(EntityAgeable ageable) {
        return new EntityAdeliePenguin(this.worldObj);
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? 0.5F : 0.9F;
    }

    private class EntityAIExtinguishFire extends EntityAIPanic {
        public EntityAIExtinguishFire() {
            super(EntityAdeliePenguin.this, 2.0);
        }

        @Override
        public boolean shouldExecute() {
            return (EntityAdeliePenguin.this.isChild() || EntityAdeliePenguin.this.isBurning()) && super.shouldExecute();
        }
    }
}