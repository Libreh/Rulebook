package me.libreh.rulebook.mixin;

import com.mojang.authlib.GameProfile;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import me.libreh.rulebook.config.Config;
import me.libreh.rulebook.gui.RulebookGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.libreh.rulebook.Rulebook.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private final ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);

    @Unique
    private final UUID playerUuid = this.getUuid();

    @Unique
    private int rulebookTick;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        rulebookTick++;
        if (rulebookTick == 4) {
            if (JOIN_LIST.contains(playerUuid)) {
                if (!Config.hasAccepted(player)) {
                    if (!RULEBOOK_LIST.contains(playerUuid)) {
                        if (!(player.currentScreenHandler instanceof BookScreenHandler)) {
                            RULEBOOK_LIST.add(playerUuid);
                            openBookGui(player);
                        }
                    }
                } else {
                    JOIN_LIST.remove(playerUuid);
                    RULEBOOK_LIST.remove(playerUuid);
                    player.closeHandledScreen();
                }
            }

            rulebookTick = 0;
        }
    }

    @Unique
    private void openBookGui(ServerPlayerEntity player) {
        var rulesArray = generateBookPages();
        var bookBuilder = new BookElementBuilder();
        for (var rule : rulesArray) {
            bookBuilder.addPage(rule);
        }
        bookBuilder.addPage(Placeholders.parseText(TextParserUtils.formatText(Config.getConfig().finalPage), PlaceholderContext.of(player)));
        new RulebookGui(player, bookBuilder).open();
    }


    @Unique
    private List<Text> generateBookPages() {
        List<Text> rulesList = new ArrayList<>();

        String header = Config.getConfig().rulesHeader;
        String schema = Config.getConfig().ruleSchema;

        var rules = Config.getConfig().rules;

        for (int index = 0; index < rules.size(); index++) {
            var rule = rules.get(index);
            String ruleTitle = rule.title;
            String ruleDescription = rule.description;

            String ruleBuilder = header + "\n" + parseRule(schema, index + 1, ruleTitle, ruleDescription);

            rulesList.add(Placeholders.parseText(TextParserUtils.formatText(ruleBuilder), PlaceholderContext.of(((player)))));
        }

        return rulesList;
    }
}
