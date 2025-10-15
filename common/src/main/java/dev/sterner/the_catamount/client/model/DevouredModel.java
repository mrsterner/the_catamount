package dev.sterner.the_catamount.client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.entity.DevouredEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class DevouredModel
        extends HierarchicalModel<DevouredEntity>
        implements ArmedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart head;

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TheCatamount.id("devoured"), "main");

    public DevouredModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
        this.head = this.root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, -2.5f, 0.0f));
        partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 20.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 16).addBox(-1.5f, 1.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(-0.2f)), PartPose.offset(0.0f, 20.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-1.25f, -0.5f, -1.0f, 2.0f, 4.0f, 2.0f, new CubeDeformation(-0.1f)), PartPose.offset(-1.75f, 0.25f, 0.0f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.75f, -0.5f, -1.0f, 2.0f, 4.0f, 2.0f, new CubeDeformation(-0.1f)), PartPose.offset(1.75f, 0.25f, 0.0f));
        partDefinition3.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset(0.5f, 1.0f, 1.0f));
        partDefinition3.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-0.5f, 1.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(DevouredEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180);
        this.head.xRot = headPitch * ((float)Math.PI / 180);
        float f = Mth.cos(ageInTicks * 5.5f * ((float)Math.PI / 180)) * 0.1f;
        this.rightArm.zRot = 0.62831855f + f;
        this.leftArm.zRot = -(0.62831855f + f);
        if (entity.isCharging()) {
            this.body.xRot = 0.0f;
            this.setArmsCharging(entity.getMainHandItem(), entity.getOffhandItem(), f);
        } else {
            this.body.xRot = 0.15707964f;
        }
        this.leftWing.yRot = 1.0995574f + Mth.cos(ageInTicks * 45.836624f * ((float)Math.PI / 180)) * ((float)Math.PI / 180) * 16.2f;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.leftWing.xRot = 0.47123888f;
        this.leftWing.zRot = -0.47123888f;
        this.rightWing.xRot = 0.47123888f;
        this.rightWing.zRot = 0.47123888f;
    }

    private void setArmsCharging(ItemStack rightHandItem, ItemStack leftHandItem, float f) {
        if (rightHandItem.isEmpty() && leftHandItem.isEmpty()) {
            this.rightArm.xRot = -1.2217305f;
            this.rightArm.yRot = 0.2617994f;
            this.rightArm.zRot = -0.47123888f - f;
            this.leftArm.xRot = -1.2217305f;
            this.leftArm.yRot = -0.2617994f;
            this.leftArm.zRot = 0.47123888f + f;
            return;
        }
        if (!rightHandItem.isEmpty()) {
            this.rightArm.xRot = 3.6651914f;
            this.rightArm.yRot = 0.2617994f;
            this.rightArm.zRot = -0.47123888f - f;
        }
        if (!leftHandItem.isEmpty()) {
            this.leftArm.xRot = 3.6651914f;
            this.leftArm.yRot = -0.2617994f;
            this.leftArm.zRot = 0.47123888f + f;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        boolean bl = side == HumanoidArm.RIGHT;
        ModelPart modelPart = bl ? this.rightArm : this.leftArm;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(0.55f, 0.55f, 0.55f);
        this.offsetStackPosition(poseStack, bl);
    }

    private void offsetStackPosition(PoseStack poseStack, boolean rightSide) {
        if (rightSide) {
            poseStack.translate(0.046875, -0.15625, 0.078125);
        } else {
            poseStack.translate(-0.046875, -0.15625, 0.078125);
        }
    }
}