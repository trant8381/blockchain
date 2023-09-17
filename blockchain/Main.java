package blockchain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import blockchain.Text;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        List<Block> blockList = new ArrayList<>();
        Blockchain bc = new Blockchain(blockList);
        
        Text text = new Text();
        text.initTexts();
        Minerchain minerchain = new Minerchain(blockList, 6, text);

        for (long i = 1; i < 16; i++) {
            minerchain.createBlock(i);
        }
    }
}

class Blockchain {
    private final List<Block> blockList;

    Blockchain(List<Block> blockList) {
        this.blockList = blockList;
    }

    public void append(Block block) {
        this.blockList.add(block);
    }
}
class Block {
    private final Long blockId;
    private final String prevHash;
    private final int zeros;
    private static long duration;
    private final int minerId;
    private final Object message;

    private String curHash = "0";
    private final long timestamp = new Date().getTime();

    public Block(String prevHash, int zeros, int miner, long blockID, String message) {
        this.zeros = zeros;
        this.prevHash = prevHash;
        this.blockId = blockID;
        this.minerId = miner;
        this.message = message;
        long start = Instant.now().getEpochSecond();
        this.curHash = initCurHash();
        long end = Instant.now().getEpochSecond();

        setDuration(end - start);
    }

    public Block(int zeros, int miner, long blockID, String message) {
        this.zeros = zeros;
        this.blockId = blockID;
        prevHash = "0";
        this.curHash = initCurHash();
        this.minerId = miner;
        this.message = "no message";
    }

    public static void setDuration(long duration) {
        Block.duration = duration;
    }

    @Override
    public String toString() {
        return String.format("Block:%nCreated by miner%d%nminer%d gets 100 VC%nId: %d%nTimestamp: %d%nMagic number: %d%nHash of the previous block:%n%s%nHash of the block:%n%s%nBlock data: %s%nBlock was generating for %d seconds%n",
                this.minerId, this.minerId , blockId, timestamp, (long) Math.floor(Math.random()*(332132132323232L+1)), prevHash, this.curHash, this.message, duration);
    }

    public long getDuration() {
        return Block.duration;
    }

    public String getCurHash() {
        return curHash;
    }

    public String initCurHash() {
        StringBuilder zeroString = new StringBuilder();
        zeroString.append("0".repeat(Math.max(0, this.zeros)));
        {
            do {
                curHash = StringUtil.applySha256(this.toString());
            } while (!(curHash.startsWith(String.valueOf(zeroString)) && !curHash.startsWith(zeroString + "0")));
        }
        return curHash;
    }
}

class StringUtil {
    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class Minerchain {
    private final List<Block> blockList;
    private final int numMiners;
    private final Text text;
    private int nz;

    public Minerchain(List<Block> blockList, int numMiners, Text text) {
        this.blockList = blockList;
        this.numMiners = numMiners;
        this.nz = 0;
        this.text = text;
    }

    public void createBlock(long blockId) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(this.numMiners);
        List<Callable<Block>> minerList = new ArrayList<>();

        for (int i = 1; i < this.numMiners + 1; i++) {
            Callable<Block> worker = new Miner(blockList, i, blockId, this.nz, this.text.next());
            minerList.add(worker);
        }
        Block result = executor.invokeAny(minerList);
        executor.shutdownNow();
        System.out.print(result);
        blockList.add(result);

        if (result.getDuration() < 0) {
            this.nz += 1;
            System.out.printf("N was increased to %d%n%n", this.nz);
        } else if (result.getDuration() > 1) {
            this.nz -= 1;
            System.out.printf("N was decreased to %d%n%n", this.nz);
        } else {
            System.out.println("N stays the same\n");
        }
    }
}

class Miner implements Callable<Block> {
    private final int id;
    private final List<Block> blockList;
    private final int nz;
    private final long blockId;
    private final String message;

    public Miner(List<Block> blocklist, int id, long blockID, int nz, String message) {
        this.blockList = blocklist;
        this.id = id;
        this.nz = nz;
        this.blockId = blockID;
        this.message = message;
    }

    @Override
    public Block call() {
        Block newBlock;
        if (this.blockList.size() == 0) {
            newBlock = new Block(this.nz, this.getId(), this.blockId, this.message);
        } else {
            newBlock = new Block(this.blockList.get(this.blockList.size() - 1).getCurHash(), this.nz, this.getId(), this.blockId, message);
        }
        return newBlock;
    }

    public int getId() {
        return id;
    }
}