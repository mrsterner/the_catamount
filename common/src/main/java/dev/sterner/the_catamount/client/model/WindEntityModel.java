package dev.sterner.the_catamount.client.model;


import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.entity.WindEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

@Environment(value= EnvType.CLIENT)
public class WindEntityModel extends HierarchicalModel<WindEntity> {
    private final ModelPart root;
    private final ModelPart wind;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TheCatamount.id("wind"), "main");

    public WindEntityModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        this.wind = root.getChild("wind_body");
        this.windBottom = this.wind.getChild("wind_bottom");
        this.windMid = this.windBottom.getChild("wind_mid");
        this.windTop = this.windMid.getChild("wind_top");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition5 = partDefinition.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition partDefinition6 = partDefinition5.addOrReplaceChild("wind_bottom", CubeListBuilder.create().texOffs(1, 83).addBox(-2.5f, -7.0f, -2.5f, 5.0f, 7.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition partDefinition7 = partDefinition6.addOrReplaceChild("wind_mid", CubeListBuilder.create().texOffs(74, 28).addBox(-6.0f, -6.0f, -6.0f, 12.0f, 6.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(78, 32).addBox(-4.0f, -6.0f, -4.0f, 8.0f, 6.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(49, 71).addBox(-2.5f, -6.0f, -2.5f, 5.0f, 6.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -7.0f, 0.0f));
        partDefinition7.addOrReplaceChild("wind_top", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -8.0f, -9.0f, 18.0f, 8.0f, 18.0f, new CubeDeformation(0.0f)).texOffs(6, 6).addBox(-6.0f, -8.0f, -6.0f, 12.0f, 8.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(105, 57).addBox(-2.5f, -8.0f, -2.5f, 5.0f, 8.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -6.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    /**
     * Sets this entity's model rotation angles
     */
    @Override
    public void setupAnim(WindEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float f = ageInTicks * (float)Math.PI * -0.1f;
        this.windTop.x = Mth.cos(f) * 1.0f * 0.6f;
        this.windTop.z = Mth.sin(f) * 1.0f * 0.6f;
        this.windMid.x = Mth.sin(f) * 0.5f * 0.8f;
        this.windMid.z = Mth.cos(f) * 0.8f;
        this.windBottom.x = Mth.cos(f) * -0.25f * 1.0f;
        this.windBottom.z = Mth.sin(f) * -0.25f * 1.0f;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public ModelPart wind() {
        return this.wind;
    }
}

