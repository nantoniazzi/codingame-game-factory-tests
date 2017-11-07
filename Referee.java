import java.util.ArrayList;
import java.util.List;

class PongPlayerInputs implements PlayerInputs {
    int meY;
    int opponentY;
    int ballX;
    int ballY;
}

class PongPlayerActions implements PlayerActions {
    int desiredY;
}

class PongPlayer extends Player<PongPlayerInputs, PongPlayerActions> {

    Sprite sprite;

    @Override
    PongPlayerActions deserialize(List<String> actions) {
        PongPlayerActions playerActions = new PongPlayerActions();
        playerActions.desiredY = Integer.valueOf(actions.get(0));
        return playerActions;
    }

    @Override
    List<String> serialize(PongPlayerInputs inputs) {
        List<String> ret = new ArrayList<>();
        ret.add(String.valueOf(inputs.meY));
        ret.add(String.valueOf(inputs.opponentY));
        ret.add(inputs.ballX + " " + inputs.ballY);
        return ret;
    }
}

class PongReferee extends Referee<PongPlayer> {

    Sprite ball;

    @Override
    void load() {
        resourcesManager.loadConfig("https://www.codingame.com/game-factory/default.json");
        resourcesManager.registerAndLoadTexture("bar", "https://i.imgur.com/WJ475sa.jpg");
        resourcesManager.registerAndLoadTexture("wall", "https://i.imgur.com/PO788sa.jpg");
        resourcesManager.registerAndLoadTexture("logo", "https://i.imgur.com/44788sa.jpg");
        resourcesManager.loadTexture("ball");
        resourcesManager.loadFont("defaultFont");
        resourcesManager.registerAndLoadReplay("replay1", 548451);
        resourcesManager.registerAndLoadReplay("replay2", 548452);
    }

    /**
     * Executed when the viewer is loaded
     */
    public void init() {
        //replayManager.playLoop("replay1", "replay2");
        //world.showSplashLogo("logo");
    }

    /**
     * Executed when the game is played
     */
    @Override
    void start() {
        gameManager.setExpectedNbLineOutput(1);

        world.setSize(1000, 1000);
        //world.setGravity(0);

        ball = world.createSprite();
        ball.setPosition(world.getWidth() / 2, world.getHeight() / 2);
        ball.setTexture("ball", 0.5, 0.5);
        ball.setScale(1.0);
        //ball.enableBody(true);
        //ball.getBody().setSpeed(new Vector(1.0, 1.0));
        //ball.getBody().setMass(1.0);
        //ball.getBody().addShape(new BodyCircleShape(0, 0, 1.0));

        Sprite topWall = world.createSprite();
        topWall.setPosition(0, 0);
        topWall.setTexture("wall");
        //topWall.enableBody(true);        
        //topWall.getBody().addShape(new BodyRectShape(0, 0, world.getWidth(), 5));

        Sprite bottomWall = world.createSprite();
        bottomWall.setPosition(world.getHeight() - 5, 0);
        bottomWall.setTexture("wall");
        //bottomWall.enableBody(true);
        //bottomWall.getBody().addShape(new BodyRectShape(0, 0, world.getWidth(), 5));

        PongPlayer p0 = gameManager.getPlayer(0);
        p0.sprite = world.createSprite();
        p0.sprite.setPosition(10, world.getHeight() / 2);
        p0.sprite.setColor("0xff0000");
        p0.sprite.setTexture("bar", 0.5, 0.5);
        //p0.enableBody(true);        
        //p0.getBody().addShape(new BodyRectShape(-5, -20, 5, 20));

        PongPlayer p1 = gameManager.getPlayer(0);
        p1.sprite = world.createSprite();
        p1.sprite.setPosition(world.getWidth() - 10, world.getHeight() / 2);
        p1.sprite.setColor("0x00ff00");
        p1.sprite.setTexture("bar", 0.5, 0.5);
        //p1.enableBody(true);        
        //p1.getBody().addShape(new BodyRectShape(-5, -20, 5, 20));
    }

    /**
     * Game loop
     */
    @Override
    void update() {
        // Read players actions
        for (PongPlayer p : gameManager.getActivePlayers()) {
            PongPlayerActions actions;
            try {
                actions = p.readActions(50); // blocking (max timeout of 50 ms)
                int deltaMove = actions.desiredY - p.sprite.getY();
                deltaMove = clamp(deltaMove, -20, 20);
                p.sprite.setY(clamp(p.sprite.getY() + deltaMove, 20, world.getHeight() - 20));
            } catch (ReadActionsException | PlayerTimeoutException e) {
            }
        }

        // Flush entities update
        world.update();

        if (ball.getX() < 0) {
            gameManager.getPlayer(0).die("give the reason explanation...");
        } else if (ball.getX() > world.getWidth()) {
            gameManager.getPlayer(1).die("give the reason explanation...");
        }

        // Send new inputs with the updated positions
        for (PongPlayer p : gameManager.getActivePlayers()) {
            PongPlayerInputs inputs = new PongPlayerInputs();
            inputs.meY = p.sprite.getY();
            inputs.opponentY = gameManager.getPlayer((p.getIndex() + 1) % 2).sprite.getY();
            inputs.ballX = ball.getX();
            inputs.ballY = ball.getY();
            p.sendInputs(inputs);
        }
    }
}