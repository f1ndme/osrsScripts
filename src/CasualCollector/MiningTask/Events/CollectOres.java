package CasualCollector.MiningTask.Events;

import CasualCollector.FrameWork.EventBase;
import CasualCollector.Operators.Mine;

public class CollectOres extends EventBase {
    public CollectOres() {
        addOperator(new Mine());
        operatorChanged();
    }

    public boolean operating() {
        return super.operating();
    }

}
