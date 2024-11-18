package gg.discord.mrkk.tadeu.coinflip.hooks;

import lombok.Getter;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;

@Getter
public class CashIntegration {

    private PlayerPointsAPI ppAPI;

    public void loadPpAPI() {
        this.ppAPI = PlayerPoints.getInstance().getAPI();
    }
}
