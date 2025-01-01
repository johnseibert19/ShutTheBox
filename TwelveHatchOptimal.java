import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwelveHatchOptimal {
    private static final double[] dieRollChances = {0, 0, 1.0/36, 1.0/18, 1.0/12, 1.0/9, 5.0/36, 1.0/6, 5.0/36, 1.0/9, 1.0/12, 1.0/18, 1.0/36};
    private static final Double[] cache = new Double[1 << 12];

    static {
        Arrays.fill(cache, null);
        cache[0] = 1.0;
    }

    public static int flip(int input, int n) {
        if (0 <= n && n <= 12) {
            int flipBit = n - 1;
            return input - (1 << flipBit);
        }
        throw new IllegalArgumentException("Invalid block number");
    }

    public static boolean isFlipped(int input, int n) {
        if (0 <= n && n <= 12) {
            int flipBit = n - 1;
            return (input >> flipBit & 1) == 1;
        }
        throw new IllegalArgumentException("Invalid block number");
    }

    public static List<List<Integer>> getPossiblePredecessors(int state) {
        int copy = state;
        List<List<Integer>> output = new ArrayList<>();
        List<Integer> posFlips = new ArrayList<>();
        for (int i = 0; copy != 0; i++) {
            if (copy % 2 == 1) {
                posFlips.add(i);
            }
            copy = copy >> 1;
        }

        if (posFlips.isEmpty()) {
            return output;
        }

        for (int aaa = 0; aaa < 1 << posFlips.size(); aaa++) {
            List<Integer> posCombination = new ArrayList<>();
            int sum = 0;
            for (int aab = 0; aab < posFlips.size(); aab++) {
                if ((aaa & (1 << aab)) == 1 << aab) {
                    posCombination.add(posFlips.get(aab));
                    sum += posFlips.get(aab) + 1;
                }
            }

            if (sum >= 2 && sum <= 12) {
                output.add(new ArrayList<>(posCombination));
            }
        }

        return output;
    }

    public static int getPredecessor(int cur, List<Integer> flips) {
        for (int flip : flips) {
            cur -= 1 << flip;
        }
        return cur;
    }

    public static int getSumFromFlipCombination(List<Integer> flips) {
        int cur = 0;
        for (int flip : flips) {
            cur += flip + 1;
        }
        return cur;
    }

    public static double winChance(int state, int throwCount) {
        if (throwCount == 0) {
            return cache[state] != null ? cache[state] : throwNewDie(state);
        } else {
            double result = 0;
            for (int i = 1; i <= throwCount; i++) {
                if (isFlipped(state, i)) {
                    result = Math.max(result, winChance(flip(state, i), throwCount - i));
                }
            }
            return result;
        }
    }

    public static double throwNewDie(int state) {
        if (cache[state] == null) {
            double newValue = 0;
            for (int i = 2; i <= 12; i++) {
                double val = winChance(state, i);
                newValue += val * dieRollChances[i];
            }
            cache[state] = newValue;
        }
        return cache[state];
    }

    public static void main(String[] args) {
        int startValue = 0b111111111111; // 12 True False values represented as number (4095)
        double winChance = throwNewDie(startValue);

        try {
            FileWriter writer = new FileWriter("12HatchOptimal.txt");
            writer.write(String.format("%-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s\n",
                    "State", "Win Chance", "Roll 2", "Roll 3", "Roll 4", "Roll 5", "Roll 6", "Roll 7", "Roll 8", "Roll 9", "Roll 10", "Roll 11", "Roll 12"));
            for (int state = 0; state < (1 << 12); state++) {
                if (cache[state] != null) {
                    writer.write(String.format("%-15s %-15.5f", stateToString(state), cache[state]));
                    for (int roll = 2; roll <= 12; roll++) {
                        writer.write(String.format(" %-15s", getOptimalMove(state, roll)));
                    }
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Win chance for the starting state: " + winChance);
    }

    public static String getOptimalMove(int state, int roll) {
        List<List<Integer>> possiblePredecessors = getPossiblePredecessors(state);
        double maxWinChance = -1;
        List<Integer> optimalMove = null;

        for (List<Integer> flips : possiblePredecessors) {
            if (getSumFromFlipCombination(flips) == roll) {
                int predecessor = getPredecessor(state, flips);
                double winChance = cache[predecessor] != null ? cache[predecessor] : throwNewDie(predecessor);
                if (winChance > maxWinChance) {
                    maxWinChance = winChance;
                    optimalMove = flips;
                }
            }
        }

        if (optimalMove != null) {
            for (int i = 0; i < optimalMove.size(); i++) {
                optimalMove.set(i, optimalMove.get(i) + 1);
            }
        }

        return optimalMove != null ? optimalMove.toString() : "None";
    }

    public static String stateToString(int state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append((state & (1 << i)) != 0 ? "1" : "0");
        }
        return sb.reverse().toString();
    }
}