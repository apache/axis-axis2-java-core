package axis2;

import java.util.*;

/**
 * ExecutionChain
 */
public class ExecutionChain {
    List phases;
    Map phasesByName;
    Phase currentPhase;
    int phaseIndex = 0;
    int handlerIndex = 0;

    public ExecutionChain() {
        phases = new LinkedList();
        phasesByName = new HashMap();
    }

    public void addHandler(Handler handler) {
        if (currentPhase == null) {
            // Make a new anonymous Phase?
        }
        currentPhase.addHandler(handler);
    }

    public void addPhase(String name, Phase phase) {
        phases.add(phase);
        currentPhase = phase;
        phasesByName.put(name, phase);
    }

    public void addHandlerToPhase(String phaseName, Handler handler) throws Exception {
        Phase phase = (Phase)phasesByName.get(phaseName);
        if (phase == null) {
            throw new Exception("No such phase!");
        }
        phase.addHandler(handler);
    }

    public void reset() {
        currentPhase = (Phase)phases.get(0);
    }

    public boolean invoke(MessageContext context) throws Exception {
        int ret = 0;
        while (true) {
            ret = currentPhase.step(context, handlerIndex);
            if (ret == -1) {
                return false; // paused
            }

            if (ret == 0) {
                // done with this phase.
                handlerIndex = 0;
                phaseIndex++;
                if (phases.size() == phaseIndex)
                    return true;  // finished
                currentPhase = (Phase)phases.get(phaseIndex);
                continue;
            }

            handlerIndex = ret;
        }
    }

    /**
     * Convenience method to add all Handlers in a list
     * @param handlers
     */
    public void addHandlers(List handlers) {
        for (Iterator i = handlers.iterator(); i.hasNext();) {
            Handler handler = (Handler) i.next();
            addHandler(handler);
        }
    }

    /**
     * Convenience method to add Handlers to a given Phase from a List
     *
     * @param phaseName
     * @param handlers
     * @throws Exception
     */
    public void addHandlersToPhase(String phaseName, List handlers) throws Exception {
        Phase phase = (Phase)phasesByName.get(phaseName);
        if (phase == null) {
            throw new Exception("No such phase '" + phaseName + "'");
        }
        for (Iterator i = handlers.iterator(); i.hasNext();) {
            Handler handler = (Handler) i.next();
            phase.addHandler(handler);
        }
    }
}
