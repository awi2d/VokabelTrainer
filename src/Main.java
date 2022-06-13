import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.EnumSet;

public class Main {
    //static final String dirname = "C:\\Users\\Idefix\\Documents\\uni\\s6\\TürkischA1\\Vokabeln\\";
    static final String dirname = "Vokabeln\\";
    static Scanner in;

    public static void main(String[] args){

        File folder = new File(Main.dirname);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles == null || listOfFiles.length == 0){
            System.out.println("the direcotry "+folder.getAbsolutePath()+" is empty.");
            return;
        }

        //remove all files ending in - Kopie.txt from listOfFiles
        int wp=0;
        for(int i=0; i<listOfFiles.length; i++){
            if(!listOfFiles[wp].getName().endsWith(" - Kopie.txt")){
                wp++;
            }
            listOfFiles[wp] = listOfFiles[i];
        }
        listOfFiles = Arrays.copyOf(listOfFiles, wp+1);

        while(true){
            for(File file:listOfFiles){
                String fileName = file.getName();
                System.out.print("file: "+fileName);
                List<Vocab> vocabs = Main.readfile(file.getAbsolutePath());
                if(vocabs == null){
                    System.out.println("-total NaN-todo NaN");
                }else{
                    System.out.println("-total "+vocabs.size()+"-todo "+Main.getTodo(vocabs).size());
                }
            }

            Main.in = new Scanner(System.in);
            in.useDelimiter("\n");

            //parseinput
            String fn = in.next();
            String[] cmdspplit = fn.split(" ");
            if("exit".equals(cmdspplit[0])){
                return;
            }
            if("reset".equals(cmdspplit[0])){
                String filename = folder.getAbsolutePath()+"\\"+cmdspplit[1]+".txt";
                Main.resetfile(filename);
            }
            if("check".equals(cmdspplit[0])){
                List<Vocab> vocabs = new ArrayList<>();
                int editDist = Integer.parseInt(cmdspplit[1]);
                for(int i=2; i<cmdspplit.length; i++){
                    String filename = folder.getAbsolutePath() + "\\" + cmdspplit[i] + ".txt";
                    List<Vocab> tmp = Main.readfile(filename);
                    if(tmp!=null){
                        vocabs.addAll(tmp);
                    }else{
                        System.out.println("File "+filename+" contained no Vocabs");
                    }
                }
                Main.printDuplicates(vocabs, editDist, false);
            }
            if("run".equals(cmdspplit[0])){
                if(cmdspplit.length == 2) {
                    String filename = folder.getAbsolutePath() + "\\" + cmdspplit[1] + ".txt";
                    run(filename);
                }else{
                    if("tag".equals(cmdspplit[1])){
                        Vocab.Tags tag = Vocab.Tags.valueOf(cmdspplit[2]);
                        LinkedList<Vocab> voctag = new LinkedList<>();
                        for(File file:listOfFiles){
                            List<Vocab> vocabs = Main.readfile(file.getAbsolutePath());
                            if(vocabs != null) {
                                for (Vocab voc : vocabs) {
                                    if (voc.tags.contains(tag)) {
                                        voctag.add(Vocab.fromString(voc.toString()));//copy voc to make sure nothing is saved
                                    }
                                }
                            }
                        }
                        System.out.println("there are "+voctag.size()+" Vocab with tag "+tag);
                        Collections.shuffle(voctag);
                        run(voctag);
                    }
                }
            }
            if("help".equals(cmdspplit[0])){
                System.out.println("TODO add Documentation. until then look into Main.main");
            }
        }
    }

    public static int editdist(String a, String b){
        return distance(a, b, 0, 0);
    }

    public static class Tupel{
        public final String w1;
        public final int w1pos;
        public final String w2;
        public final int w2pos;

        public Tupel(String a, int b, String c, int d){
            this.w1 = a;
            this.w1pos = b;
            this.w2 = c;
            this.w2pos = d;
        }

        @Override
        public boolean equals(Object obj){
            try {
                Tupel o = (Tupel) obj;
                if(this.w1.length()-this.w1pos != o.w1.length()-o.w1pos|| this.w2.length()-this.w2pos != o.w2.length()-o.w2pos){
                    return false;
                }
                for(int i=0; i<this.w1.length()-this.w1pos; i++){
                    if(this.w1.charAt(this.w1pos+i) != o.w1.charAt(o.w1pos+i)){
                        return false;
                    }
                }
                for(int i=0; i<this.w2.length()-this.w2pos; i++){
                    if(this.w2.charAt(this.w2pos+i) != o.w2.charAt(o.w2pos+i)){
                        return false;
                    }
                }

                return true;
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public int hashCode(){
            //may have way more colisions then necesary, cause content of word is ignored
            return this.w1pos^this.w2pos<<8^this.w1.length()<<16^this.w2.length()<<24;
        }
    }

    private static final Map<Tupel, Integer> editDistanceCache = new HashMap<>();
    private static int distance(String s1, String s2, int i, int j) {
        if(editDistanceCache.containsKey(new Tupel(s1, i, s2, j))){
            return editDistanceCache.get(new Tupel(s1, i, s2, j));
        }
        //System.out.println("calculate edit distance from "+s1+"["+i+":] to "+s2+"["+j+":]");
        if (j == s2.length()) {
            return s1.length() - i;
        }
        if (i == s1.length()) {
            return s2.length() - j;
        }
        int r;
        if (s1.charAt(i) == s2.charAt(j)){
            r = distance(s1, s2, i + 1, j + 1);
        }else{
            int rep = distance(s1, s2, i + 1, j + 1) + 1;
            int del = distance(s1, s2, i, j + 1) + 1;
            int ins = distance(s1, s2, i + 1, j) + 1;
            r = Math.min(del, Math.min(ins, rep));
        }

        editDistanceCache.put(new Tupel(s1, i, s2, j), r);
        return r;
    }

    private static String parseInput_ans(){
        String input = in.next();
        while(input == null || input.length() == 0){
            input = in.next();
        }
        Map<String, String> replacements = Map.ofEntries(Map.entry("î", "ı"), Map.entry("^g", "ğ"), Map.entry("^s", "ş"), Map.entry("^c", "ç"));
        for (String key : replacements.keySet()) {
            input = input.replace(key, replacements.get(key));
        }
        return input;
    }

    private static void run(List<Vocab> vocabs){
        //like run, but doesnt save results
        LinkedList<Vocab> todolist = Main.getTodo(vocabs);
        Random r = new Random();
        while (todolist.size()>0){
            Vocab voc = todolist.getFirst();
            int q = voc.todo(r);
            if (q >= 0) {
                System.out.println(voc.getQuestion(q));
                String input = Main.parseInput_ans();
                System.out.println(input + " ?= " + voc.relation[1 - q]+" "+voc.tags+" distance = "+Main.editdist(input, voc.relation[1-q]));  //TODO only works when relation.length == 2
                input = in.next();
                while (input == null || !("y".equals(input) || input.equals("n"))){
                    input = in.next();
                }
                if("n".equals(input)){
                    int i = 2+r.nextInt(3);//{2, 3, 4}
                    if(todolist.size()>i+1){
                        todolist.add(i, voc);
                    }else{
                        todolist.addLast(voc);
                    }
                }
                todolist.remove(0);
            }
        }
    }

    private static void run(String filename){
        List<Vocab> vokab = Main.readfile(filename);
        if(vokab == null){
            System.out.println("filename "+filename+" points to empty/malformated file.");
            return;
        }
        LinkedList<Vocab> todolist = Main.getTodo(vokab);
        Random r = new Random();
        while(todolist.size()>0){
            Vocab voc = todolist.getFirst();
            int q = voc.todo(r);
            if (q >= 0) {
                System.out.println(voc.getQuestion(q));
                String input = Main.parseInput_ans();
                System.out.println(input + " ?= " + voc.relation[1 - q]+" "+voc.tags+" distance = "+Main.editdist(input, voc.relation[1-q]));  //TODO only works when relation.length == 2
                //for(Vocab other:vokab){
                //    if(Main.editdist(other.relation[1-q], input) < 2){
                //        System.out.println("did you mean: "+other);
                //    }
                //}
                input = in.next();
                while (input == null){
                    input = in.next();
                }
                while(input.startsWith("-")){
                    // parse as command
                    String[] cmd = input.split(" ");
                    System.out.println("cmd = "+Arrays.toString(cmd));
                    if("-tag".equals(cmd[0])){
                        if("add".equals(cmd[1])){
                            if(cmd.length!=3){
                                System.out.println("invald command syntax: \"- tag add name\" does take exacly 3 arguments");
                            }else{
                                System.out.println("adding tag \""+cmd[2]+"\" was successful = "+voc.addTag(cmd[2]));
                            }
                        }
                        if("list".equals(cmd[1])){
                            System.out.println("all Tags: "+Arrays.toString(Vocab.Tags.values()));
                        }
                    }
                    if("-exit".equals(cmd[0])){
                        writeFile(filename, vokab);
                        return;
                    }
                    input = in.next();
                    while (input == null){
                        input = in.next();
                    }
                }
                while (input == null || !("y".equals(input) || input.equals("n"))){
                    input = in.next();
                }
                voc.ans(input.equals("y"), q);
                if(voc.todo(q)){
                    int i = 2+r.nextInt(3);//{2, 3, 4}
                    if(todolist.size()>i+1){
                        todolist.add(i, voc);
                    }else{
                        todolist.addLast(voc);
                    }
                }
                todolist.remove(0);
                writeFile(filename, vokab);
            }
        }
        System.out.println("finished all Vocabs from "+filename+" for today");
    }

    public static void printDuplicates(List<Vocab> all, int editDist, boolean doEdit){
        //merges duplicate Vocabs
        for (int i=0; i+1<all.size(); i++) {
            for (int ii=i+1; ii<all.size(); ii++) {
                Vocab v0 = all.get(i);
                Vocab v1 = all.get(ii);
                for(int r=0; r<v0.relation.length; r++) {
                    if(Main.editdist(v0.relation[r], v1.relation[r]) < editDist) {
                        System.out.println("duplicate Vocab: relation["+r+"] hat edit dist "+Main.editdist(v0.relation[r], v1.relation[r])+"\n" + v0 + "#" + v0.filename + "\n" + v1 + "#" + v1.filename + "\n");
                        if(doEdit) {
                            all.remove(v0);
                            all.remove(v1);
                            String[] relation = v0.relation;
                            LocalDate[] next = new LocalDate[v0.next.length];
                            for (int n = 0; n < next.length; n++) {
                                next[n] = LocalDate.now();
                            }
                            int[] correctanswers = new int[v0.correctanswers.length];
                            for (int c = 0; c < correctanswers.length; c++) {
                                correctanswers[c] = Math.min(v0.correctanswers[c], v1.correctanswers[c]);
                            }
                            EnumSet<Vocab.Tags> tags = EnumSet.noneOf(Vocab.Tags.class);
                            tags.addAll(v0.tags);
                            tags.addAll(v1.tags);
                            Vocab newv = new Vocab(relation, next, correctanswers, tags);
                            newv.filename = v0.filename;
                            all.add(newv);
                        }
                    }
                }
            }
        }

    }

    public static void resetfile(String filename){
        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("#")){
                    lines.add(line);
                }
                if(!line.startsWith("#") && !(line.length() == 0)){
                    String[] split = line.split("#");
                    if(split.length==2){
                        // line has format german-türk#tag-tag-
                        lines.add(split[0]+"#"+split[1]);
                    }else{
                        // line has format german-türk
                        lines.add(split[0]+"#");
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for(String tup:lines){
                bw.write(tup);
                bw.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.filecache.remove(filename);
    }

    private static LinkedList<Vocab> getTodo(List<Vocab> vokab){
        LinkedList<Vocab> todolist = new LinkedList<>();
        for(Vocab voc:vokab){
            if(voc.todo(0)){
                todolist.add(voc);
            }
            if(voc.todo(1)){
                todolist.add(voc);
            }
        }
        Collections.shuffle(todolist);
        return todolist;
    }

    public static Map<String, List<Vocab>> filecache = new HashMap<>();
    private static List<Vocab> readfile(String filename){
        if(Main.filecache.containsKey(filename)){
            return Main.filecache.get(filename);
        }
        try {
            List<Vocab> vokab = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // process the line.
                    if (!line.startsWith("#") && !(line.length() == 0)) {
                        Vocab tup = Vocab.fromString(line);
                        tup.filename = filename;
                        vokab.add(tup);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Main.filecache.put(filename, vokab);
            return vokab;
        } catch (IllegalArgumentException e){
            //parsing a line of filename into Vocab failed
            return null;
        }

    }

    private static void writeFile(String filename, List<Vocab> vokab){
        Main.filecache.put(filename, vokab);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for(Vocab tup:vokab){
                bw.write(tup.toString());
                bw.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
