import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.*;

public class Maze
{
    public static final int MWIDTH=30,MHEIGHT=30,BLOCK=20;  //setting room size and "zoom" with block
    public static boolean robotActive=true;
    public static final int SPEED=100;

    public static final int LEFT=4,RIGHT=8,UP=1,DOWN=2;

    //1=wall above, 2=wall below, 4=wall on left, 8=wall on right, 16=not included in maze yet
    static int[][] maze;
    static MazeComponent mazecomp;

    //current position of robot, starts top-left corner
    static int robotX=0,robotY=0;

    //true means that a "crumb" is shown in the room
    static boolean[][] crumbs;
    static boolean[][] visited;
    static int[] directions;

    // a state is an x,y coordinate, link to a parent state and direction int of most recent move
    class State {

        int x, y;
        State parent;
        int mostRecentMove;
        int movesSoFar;
        int[][] position;
        //visited = new boolean[MWIDTH][MHEIGHT];

        public State(int x, int y) {
            this.x = x;
            this.y = y;
            parent = null;
            movesSoFar = 0;
            mostRecentMove = 0;
            position = new int[MWIDTH][MHEIGHT];
            // that's it for this ?
        }

        public int manhattan() {

            int manhattanSum = 0;
            int xTotal = MWIDTH - x;
            int yTotal = MHEIGHT - y;

            manhattanSum = xTotal + yTotal;
            //System.out.println("xTotal= "+xTotal+",  yTotal= "+yTotal+",  manhattanSum= "+manhattanSum);

            return manhattanSum;
        }

        public boolean isSolved() {
            return ((x == MWIDTH-1) && (y == MHEIGHT-1));
        }

        //canMove takes a direction and gives me a true or false
        public boolean canMove(int direction) {

            if (robotX != MWIDTH - 1 || robotY != MHEIGHT - 1) {
                if (direction == UP) {
                    return ((maze[x][y] & UP) == 0);
                }
                if (direction == DOWN) {
                    return ((maze[x][y] & DOWN) == 0);
                }
                if (direction == LEFT) {
                    return ((maze[x][y] & LEFT) == 0);
                }
                if (direction == RIGHT) {
                    return ((maze[x][y] & RIGHT) == 0);
                }
            }
            return false;
        }

        //allMoves returns an int[] array of directions
        public int[] allMoves() {
            //let's count how many moves we can have
            int count = 0;
            if (canMove(UP)) count++;
            if (canMove(DOWN)) count++;
            if (canMove(LEFT)) count++;
            if (canMove(RIGHT)) count++;

            //make the array
            int[] moves = new int[count];

            //put them in the array (order is arbitrary)
            count = 0;
            if (canMove(UP)) moves[count++] = UP;
            if (canMove(DOWN)) moves[count++] = DOWN;
            if (canMove(LEFT)) moves[count++] = LEFT;
            if (canMove(RIGHT)) moves[count] = RIGHT;

            return moves;
        }

        //move takes a direction and gives me new State
        public State move(int direction) {

            //I can make a new state that is a copy of my current
            // then I can make the actual move within it
            State child = new State(robotX, robotY);
            //link it to parent
            child.parent = this;
            //child has one more move than parent
            child.movesSoFar = this.movesSoFar + 1;


            // making the move and setting mostRecentMove
            if ((direction == UP)&&(this.y>0)) {
                child.y = this.y-1;
                child.x = this.x;
                child.mostRecentMove = UP;
            }
            if ((direction == DOWN)&&(this.y<MHEIGHT-1)) {
                child.y = this.y+1;
                child.x = this.x;
                child.mostRecentMove = DOWN;
            }
            if ((direction == LEFT)&&(this.x>0)) {
                child.y = this.y;
                child.x = this.x-1;
                child.mostRecentMove = LEFT;
            }
            if ((direction == RIGHT)&&(this.x<MWIDTH-1)) {
                child.y = this.y;
                child.x = this.x+1;
                child.mostRecentMove = RIGHT;
            }
            return child;
        }

        //gives me an array of adjacent State objects
        public State[] adjacentStates() {
            //go through each valid direction, get the state, make an array of these, return it
            int[] possibleDirections = allMoves();

            //make an array of states, same size
            State[] adjacent = new State[possibleDirections.length];

            //call move on each valid direction
            for (int i = 0; i < possibleDirections.length; i++)
                adjacent[i] = move(possibleDirections[i]);

            return adjacent;
        }
    }

    // Implementing our DFS Search for the maze
    public void depthFirstSearch(State start) {

        //have a list of states.  I will add to one size and remove from the other
        ArrayList<State> stack = new ArrayList<State>();

        visited = new boolean[MWIDTH][MHEIGHT];
        //also have an arraylist of visited states so I don't repeat
        //ArrayList<State> visited = new ArrayList<State>();

        //add my starting state (0,0)
        stack.add(start);

        int itr=0;
        State current=null;

        while(stack.size()>0){
            itr++;

            //pop an element
            current = stack.remove(stack.size()-1);
            System.out.println(current.x +" "+current.y);

            //check whether it's solved, if so stop
            if(current.isSolved())
                break;

            //if not, mark as visited
            visited[current.x][current.y] = true;

            //find all the neighbors
            State[] adj = current.adjacentStates();


            for (State neighbor : adj) {
                if (!visited[neighbor.x][neighbor.y]) {
                    stack.add(neighbor);
                }
            }
        }

        // System.out.println("current x= " + current.x + " "+current.y);
        // System.out.println("aStar solved in "+itr+" iterations!");


        //retrace my steps.
        //start at end, work back to beginning
        //push into a stack so I can reverse them and go in the correct order
        Stack<State> backstep = new Stack<>();

        while(current!=null)	//stop at first state had a null parent
        {
            backstep.push(current);
            current=current.parent;
        }

        System.out.println("We need to make "+backstep.size()+" moves");
        directions = new int [backstep.size()];

        //now pop from the stack and print ... this is the correct order

        int counter=0;

        while(!backstep.isEmpty())
        {
            current=backstep.pop();
            directions[counter] = current.mostRecentMove;
            counter++;
        }

    }

    //choose the element from the queue, not at the front, but the one with the smallest heuristic value
    public void astar(State start) {

        //have a list of states.  I will add to one size and remove from the other
        ArrayList<State> stack = new ArrayList<State>();

        //have a 2D array of visited states so I don't repeat
        visited = new boolean[MWIDTH][MHEIGHT];

        //add my start state
        stack.add(start);

        int itr = 0;
        State current = null;

        //keep doing this until either solution or queue is empty (no solution)
        while (stack.size() > 0) {
            itr++;

            //pop an element: not from the front, but whoever has best (smallest) manhattan plus movesofar
            int indexofbest = 0;

            //go through stack, find better?
            for (int i = 0; i < stack.size(); i++) {

                //if i has a better manhattan plus movesSoFar, make that my index
                if (stack.get(i).manhattan() + stack.get(i).movesSoFar < stack.get(indexofbest).manhattan() + stack.get(i).movesSoFar)
                    indexofbest = i;
            }

            current = stack.remove(indexofbest);

            //check whether it's solved, if so stop.
            if (current.isSolved())
                break;

            //if not....
            //mark as visited
            visited[current.x][current.y] = true;

            //find all the neighbors
            State[] adj = current.adjacentStates();
            // State[] adj = current.allMoves();


            for (State neighbor : adj) {
                if (!visited[neighbor.x][neighbor.y]) {
                    stack.add(neighbor);
                }
            }
        }

        // System.out.println("aStar solved in "+itr+" iterations!");

        //retrace my steps.
        //start at end, work back to beginning
        //push into a stack so I can reverse them and go in the correct order
        Stack<State> backstep = new Stack<>();


        while (current != null)    //stop at first state had a null parent
        {
            backstep.push(current);
            current = current.parent;
        }

        System.out.println("We need to make " + backstep.size() + " moves");
        directions = new int[backstep.size()];

        //now pop from the stack and print ... this is the correct order.
        int counter = 0;

        while (!backstep.isEmpty()) {
            current = backstep.pop();
            directions[counter] = current.mostRecentMove;
            counter++;
        }
    }

    public static void makeMaze()
    {
        int[] blockListX = new int[MWIDTH*MHEIGHT];
        int[] blockListY = new int[MWIDTH*MHEIGHT];
        int blocks=0;
        int x,y;

        //Choose random starting block and add it to maze
        x=(int)(Math.random()*(MWIDTH-2)+1);
        y=(int)(Math.random()*(MHEIGHT-2)+1);
        maze[x][y]^=16;

        //Add all adjacent blocks to blocklist
        if (x>0)    // checking that you won't go off the left edge
        {                                   // making left adjacent to our starting position
            blockListX[blocks]=x-1;
            blockListY[blocks]=y;
            blocks++;
        }
        if (x<MWIDTH-1)     // checking that it won't be going off the right edge
        {                               // making right adjacent to our starting position
            blockListX[blocks]=x+1;
            blockListY[blocks]=y;
            blocks++;
        }
        if (y>0)    // checking to see that you won't go off the top edge
        {                                   // making top adjacent to our starting position
            blockListX[blocks]=x;
            blockListY[blocks]=y-1;
            blocks++;
        }
        if (y<MHEIGHT-1)    // making sure you don't fall off the bottom
        {                                   // making bottom adjacent to our starting position
            blockListX[blocks]=x;
            blockListY[blocks]=y+1;
            blocks++;
        }

        //approach:
        // start with a single room in maze and all neighbors of the room in the "blocklist"
        // choose a room that is not yet part of the maze but is adjacent to the maze
        // add it to the maze by breaking a wall
        // put all of its neighbors that aren't in the maze into the "blocklist"
        // repeat until everybody is in the maze
        while (blocks>0)
        {
            //choose a random block from blocklist
            int b = (int)(Math.random()*blocks);

            //find which block in the maze it is adjacent to
            //and remove that wall
            x=blockListX[b];
            y=blockListY[b];

            //get a list of all of its neighbors that aren't in the maze
            int[] dir=new int[4];
            int numdir=0;

            // if any of these have anything other than a
            //left
            if (x>0 && (maze[x-1][y]&16)==0)
            {
                dir[numdir++]=0;
            }
            //right
            if (x<MWIDTH-1 && (maze[x+1][y]&16)==0)
            {
                dir[numdir++]=1;
            }
            //up
            if (y>0 && (maze[x][y-1]&16)==0)
            {
                dir[numdir++]=2;
            }
            //down
            if (y<MHEIGHT-1 && (maze[x][y+1]&16)==0)
            {
                dir[numdir++]=3;
            }

            //choose one at random
            int d = (int)(Math.random()*numdir);
            d=dir[d];

            //tear down the wall
            //left
            if (d==0)
            {
                maze[x][y]^=LEFT;
                maze[x-1][y]^=RIGHT;
            }
            //right
            else if (d==1)
            {
                maze[x][y]^=RIGHT;
                maze[x+1][y]^=LEFT;
            }
            //up
            else if (d==2)
            {
                maze[x][y]^=UP;
                maze[x][y-1]^=DOWN;
            }
            //down
            else if (d==3)
            {
                maze[x][y]^=DOWN;
                maze[x][y+1]^=UP;
            }

            //set that block as "in the maze"
            maze[x][y]^=16;

            //remove it from the block list
            for (int j=0; j<blocks; j++)
            {
                if ((maze[blockListX[j]][blockListY[j]]&16)==0)
                {
                    for (int i=j; i<blocks-1; i++)
                    {
                        blockListX[i]=blockListX[i+1];
                        blockListY[i]=blockListY[i+1];
                    }
                    blocks--;
                    j=0;
                }
            }

            //put all adjacent blocks that aren't in the maze in the block list
            if (x>0 && (maze[x-1][y]&16)>0)
            {
                blockListX[blocks]=x-1;
                blockListY[blocks]=y;
                blocks++;
            }
            if (x<MWIDTH-1 && (maze[x+1][y]&16)>0)
            {
                blockListX[blocks]=x+1;
                blockListY[blocks]=y;
                blocks++;
            }
            if (y>0 && (maze[x][y-1]&16)>0)
            {
                blockListX[blocks]=x;
                blockListY[blocks]=y-1;
                blocks++;
            }
            if (y<MHEIGHT-1 && (maze[x][y+1]&16)>0)
            {
                blockListX[blocks]=x;
                blockListY[blocks]=y+1;
                blocks++;
            }
        }

        //remove top left and bottom right edges
//		maze[0][0]^=LEFT;    //commented out for now so that robot doesn't run out the entrance
        maze[MWIDTH-1][MHEIGHT-1]^=RIGHT;
    }

    //the robot will wander around aimlessly until it happens to stumble on the exit
    public static void doMazeRandomWalk()
    {
        int dir=RIGHT;

        while(robotX!=MWIDTH-1 || robotY!=MHEIGHT-1)
        {
            int x=robotX;
            int y=robotY;

            //choose a direction at random
            dir=new int[]{LEFT,RIGHT,UP,DOWN}[(int)(Math.random()*4)];
            //move the robot
            if((maze[x][y]&dir)==0)
            {
                if(dir==LEFT) robotX--;
                if(dir==RIGHT) robotX++;
                if(dir==UP) robotY--;
                if(dir==DOWN) robotY++;
            }

            //leave a crumb
            crumbs[x][y]=true;

            //repaint and pause momentarily
            mazecomp.repaint();
            try{ Thread.sleep(SPEED); } catch(Exception e) { }
        }
        System.out.println("Done");
    }

    // Making the rules for the robot's movement throughout the maze given a set of directions
    public static void doMazeGuided(int[] directions) {

    /*
        // converting our direction array with equivalent integer representation
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == LEFT) {
                directions[i] = 4;
            }
            if (directions[i] == RIGHT) {
                directions[i] = 8;
            }
            if (directions[i] == UP) {
                directions[i] = 1;
            }
            if (directions[i] == DOWN) {
                directions[i] = 2;
            }
        }
     */

        int counter = 0;
        //while the robot is not at the END block (-1 from each axis = bottom-right corner
        while (robotX != MWIDTH - 1 || robotY != MHEIGHT - 1) {
            int x = robotX;
            int y = robotY;


            //int[] dir = new int[directions.length];

            //dir[0] = directions[0] * 4;
            //for (int i=0; i< 4; i++) {

            while (counter < directions.length) {
                if ((maze[x][y] & directions[counter]) == 0) {
                    if (directions[counter] == LEFT) {
                        robotX--;
                        counter++;
                        break;
                    }
                    if (directions[counter] == RIGHT) {
                        robotX++;
                        counter++;
                        break;
                    }
                    if (directions[counter] == UP) {
                        robotY--;
                        counter++;
                        break;
                    }
                    if (directions[counter] == DOWN) {
                        robotY++;
                        counter++;
                        break;
                    }
                }
               counter++;
            }
            //}


            //leave a crumb
            crumbs[x][y] = true;

            //repaint and pause momentarily
            mazecomp.repaint();
            try {
                Thread.sleep(SPEED);
            } catch (Exception e) {
            }
        }
            System.out.println("Done!");
        }

    //  Drawing everything
    public static class MazeComponent extends JComponent
    {
        public void paintComponent(Graphics g)
        {
            g.setColor(Color.WHITE);
            g.fillRect(0,0,MWIDTH*BLOCK,MHEIGHT*BLOCK);
            g.setColor(new Color(100,0,0));
            for (int x=0; x<MWIDTH; x++)
            {
                for (int y=0; y<MHEIGHT; y++)
                {
                    if ((maze[x][y]&1)>0)
                        g.drawLine(x*BLOCK,y*BLOCK,x*BLOCK+BLOCK,y*BLOCK);
                    if ((maze[x][y]&2)>0)
                        g.drawLine(x*BLOCK,y*BLOCK+BLOCK,x*BLOCK+BLOCK,y*BLOCK+BLOCK);
                    if ((maze[x][y]&4)>0)
                        g.drawLine(x*BLOCK,y*BLOCK,x*BLOCK,y*BLOCK+BLOCK);
                    if ((maze[x][y]&8)>0)
                        g.drawLine(x*BLOCK+BLOCK,y*BLOCK,x*BLOCK+BLOCK,y*BLOCK+BLOCK);
                }
            }

            if (robotActive)
            {
                g.setColor(Color.BLUE);
                for (int x=0; x<MWIDTH; x++)
                {
                    for (int y=0; y<MHEIGHT; y++)
                    {
                        if (crumbs[x][y])
                            g.fillRect(x*BLOCK+BLOCK/2-1,y*BLOCK+BLOCK/2-1,2,2);
                    }
                }

                g.setColor(Color.GREEN);
                g.fillOval(robotX*BLOCK+1,robotY*BLOCK+1,BLOCK-2,BLOCK-2);
            }
        }
    }

    public Maze() {
        State state = new State(robotX,robotY);

        //maze a maze array and a crumb array
        maze=new int[MWIDTH][MHEIGHT];
        crumbs=new boolean[MWIDTH][MHEIGHT];
        //visited = new boolean[MWIDTH][MHEIGHT];
        //set each room to be surrounded by walls and not part of the maze
        for (int i=0; i<MWIDTH; i++)
            for (int j=0; j<MHEIGHT; j++)
            {
                maze[i][j]=31;
                crumbs[i][j]=false;
               // visited[i][j]=false;
            }
        // by this point, all maze indexes become 31 and crumbs spaces = to false


        //generate the maze
        makeMaze();

        //knock down up to 100 walls,  going row by row
        for(int i=0; i<100; i++)
        {
            int x=(int)(Math.random()*(MWIDTH-2));
            int y=(int)(Math.random()*MHEIGHT);
            if((maze[x][y]&RIGHT)!=0)
            {
                maze[x][y]^=RIGHT;
                maze[x+1][y]^=LEFT;
            }
        }

        JFrame f = new JFrame();
        f.setSize(MWIDTH*BLOCK+15,MHEIGHT*BLOCK+30);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("Maze!");
        mazecomp=new MazeComponent();
        f.add(mazecomp);
        f.setVisible(true);


      //  directions = new int[] {RIGHT, DOWN, DOWN, RIGHT, UP, DOWN, LEFT, RIGHT, DOWN,
        //        LEFT, RIGHT, DOWN, DOWN, DOWN, RIGHT, RIGHT, UP, RIGHT, RIGHT, DOWN, RIGHT};


        //have the robot wander around in its own thread
        if(robotActive) {
            new Thread(new Runnable(){
                public void run() {
                    //doMazeRandomWalk();
                    //State mazeRun = new State(robotX,robotY);

                    //depthFirstSearch(state);
                    astar(state);
                    doMazeGuided(directions);
                }
            }).start();
        }
    }

    public static void main(String[] args)  {
        new Maze();
    }
}






