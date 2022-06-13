import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class Vocab {
    // a Vocab represents a relation (e.g. german word to türkisch word) with all meta-informations requierd for training a natural Neural network to remember this relation given any of its parts
    //at all time relation.length == next.length == correctanswer.length should hold
    public String[] relation;//relation that should be trained
    public LocalDate[] next;// next[i] is the time of last correct answer given t0 relation[i] (as question), or time of initalisation
    public int[] correctanswers;// correctanswers[i] is the number of times the user answerd relation[i] correct since the last wrong answer to relation[i]
    public EnumSet<Tags> tags;// tags given to this Vocab. Can be used to only ask for Vocabs with specific tag
    public String filename;

    //metadata that should be read from meta.txt
    public enum Tags {gruesze, fragewort, nation, verb, essen, beruf, personalpronom}// tags are stored as strings. adding new Tags works fine, editing/removing existing ones not.
    public static final int[] pauselength = new int[]{0, 1, 2, 4, 8, 16, 32};// in days

    // a Vocab is represented as a single line in the .txt file

    public static Vocab fromString(String str){
        // gets a line from file and returns an Tupel object containing all information from that line.
        // the line has the format relation[0]-relation[1]-....#next[0]-next[1]-...#correctanswers[0]-correctanswers[1]-...#tags[0]-tags[1]
        String[] outersplit = str.replace("\t", "").split("#", -1);
        //System.out.println("split = "+ Arrays.toString(outersplit));
        if(outersplit.length==4){
            //line has form relation#tags#next#correctans, generate nothing
            String[] relation = outersplit[0].split("-");
            String[] tagsplit = outersplit[1].split("-");
            tagsplit[0] = tagsplit[0].replace(" ", "");
            String[] nextsplit = outersplit[2].split("-");
            String[] correctsplit = outersplit[3].split("-");

            if(relation.length != nextsplit.length || relation.length != correctsplit.length){
                throw new IllegalArgumentException("relation, nextsplit and correctsplit in line \""+str+"\" have to have the same length.");
            }
            int num = correctsplit.length;
            LocalDate[] next = new LocalDate[num];
            int[] correct = new int[num];
            for(int i=0; i<num; i++){
                next[i] = LocalDate.parse(nextsplit[i].replace("_", "-"));
                correct[i] = Integer.parseInt(correctsplit[i]);
            }
            EnumSet<Tags> tags = tagSetFromString(tagsplit);
            return new Vocab(relation, next, correct, tags);
        }
        if(outersplit.length==2){
            //line has form relation#tags, generate next and correctans
            String[] split = outersplit[0].split("-");
            EnumSet<Tags> tags = tagSetFromString(outersplit[1].split("-"));

            //initalise with default values
            int num = split.length;
            LocalDate[] next = new LocalDate[num];
            int[] correct = new int[num];
            for(int i=0; i<num; i++){
                next[i] = LocalDate.now();
                correct[i] = 0;
            }
            //System.out.println("Vocab("+ Arrays.toString(split) +", "+ Arrays.toString(next) +", "+ Arrays.toString(correct) +", "+tags+")");
            return new Vocab(split, next, correct, tags);
        }
        if(outersplit.length==1){
            //line has form relation, generate tags, next and corretans
            String[] split = outersplit[0].split("-");
            EnumSet<Tags> tags = EnumSet.noneOf(Tags.class);

            //initalise with default values
            int num = split.length;
            LocalDate[] next = new LocalDate[num];
            int[] correct = new int[num];
            for(int i=0; i<num; i++){
                next[i] = LocalDate.now();
                correct[i] = 0;
            }
            //System.out.println("Vocab("+ Arrays.toString(split) +", "+ Arrays.toString(next) +", "+ Arrays.toString(correct) +", "+tags+")");
            return new Vocab(split, next, correct, tags);
        }
        throw new IllegalArgumentException("unknown input format \""+str+"\" is no correct userinput of form \"relation0-relation1-...-relationn\" nor output of Tupel.toString");
    }

    public static EnumSet<Tags> tagSetFromString(String[] s){
        switch (s.length){
            case 0:{
                return EnumSet.noneOf(Tags.class);
            }
            case 1:{
                try {
                    return EnumSet.of(Tags.valueOf(s[0]));
                }catch (Exception e){
                    return EnumSet.noneOf(Tags.class);
                }
            }
            case 2:{
                return EnumSet.of(Tags.valueOf(s[0]), Tags.valueOf(s[1]));
            }
            case 3:{
                return EnumSet.of(Tags.valueOf(s[0]), Tags.valueOf(s[1]), Tags.valueOf(s[2]));
            }
            case 4:{
                return EnumSet.of(Tags.valueOf(s[0]), Tags.valueOf(s[1]), Tags.valueOf(s[2]), Tags.valueOf(s[3]));
            }
            case 5:{
                return EnumSet.of(Tags.valueOf(s[0]), Tags.valueOf(s[1]), Tags.valueOf(s[2]), Tags.valueOf(s[3]), Tags.valueOf(s[4]));
            }
            default:{
                EnumSet<Tags> tags = EnumSet.of(Tags.valueOf(s[0]), Tags.valueOf(s[1]), Tags.valueOf(s[2]), Tags.valueOf(s[3]), Tags.valueOf(s[4]));
                for(int i=5; i<s.length; i++){
                    tags.add(Tags.valueOf(s[i]));
                }
                return tags;
            }
        }
    }

    public boolean todo(int index){
        // returns if the vokabel is to be learned today
        return this.next[index].plusDays(Vocab.pauselength[this.correctanswers[index]]).isBefore(LocalDate.now().plusDays(1));
    }

    public int todo(Random r){
        // this.todo(this.todo(r)) == false, <=> this.todo(r) == -1
        if(this.todo(0)){
            if (this.todo(1)) {
                return r.nextInt(2);
            }else{
                return 0;
            }
        }else {
            if (this.todo(1)) {
                return 1;
            }
        }
        return -1;
    }

    public String getQuestion(int index){
        StringBuilder question = new StringBuilder();
        if(index==1){
            question.append("german ");
        }else{
            question.append("türkisch ");
        }
        question.append(this.correctanswers[index]);
        question.append(": ");
        question.append(this.relation[index]);
        return question.toString();
    }

    public void ans(boolean iscorrect, int inedex){
        this.next[inedex] = LocalDate.now();
        if(iscorrect){
            this.correctanswers[inedex]++;
            if(this.correctanswers[inedex]>=Vocab.pauselength.length){
                System.out.println("vocab reached final pauselength");
                this.correctanswers[inedex] = Vocab.pauselength.length-1;
            }
        }else{
            this.correctanswers[inedex] = 0;
        }

    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("-", this.relation));
        sb.append("#");
        Object[] taga = this.tags.toArray();
        for(int i=0; i<taga.length; i++){
            sb.append(taga[i]).append('-');
        }
        sb.append('#');
        for(LocalDate date: this.next){
            sb.append(date.toString().replace("-", "_")).append('-');
        }
        sb.append('#');
        for(int cor: this.correctanswers){
            sb.append(cor).append('-');
        }
        return sb.toString();
    }

    public boolean addTag(String tag){
        try{
            this.tags.add(Vocab.Tags.valueOf(tag));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    Vocab(String[] relation, LocalDate[] next, int[] correctanswers, EnumSet<Tags> tags){
        if(relation.length<2){
            throw new IllegalArgumentException("Vocab "+ Arrays.toString(relation) +" is invalid cause relation has length "+relation.length);
        }
        if(relation.length != next.length){
            throw new IllegalArgumentException("Vocab "+ Arrays.toString(relation) +" is invalid cause relation("+relation.length+") and next("+next.length+") have to have the same length ");
        }
        if(relation.length != correctanswers.length){
            throw new IllegalArgumentException("Vocab "+ Arrays.toString(relation) +" is invalid cause relation("+relation.length+") and correctanswers("+correctanswers.length+") have to have the same length ");
        }
        this.relation = relation;
        this.next = next;
        this.correctanswers = correctanswers;
        this.tags = tags;
    }
}
