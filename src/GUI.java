import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GUI {

    public List<Vocab> showBatch(List<Vocab> vokab){
        JFrame frame = new JFrame("vokab trainer: showBatch");
        frame.setSize(750, 500);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel question = new JLabel(vokab.get(0).relation[1]);
        JTextField answer = new JTextField();
        panel.add(question, c);
        c.gridy=1;
        panel.add(answer, c);
        JButton submit = new JButton("submit");
        int i = 0;
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ans = answer.getText();
                int wascorrect = JOptionPane.showConfirmDialog(frame, "is your answer "+ans+" the same as "+vokab.get(i).relation[0], "was your answer correct?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                // wascorrect == 0 <=> yes
                System.out.println("your input : "+ans+" is "+wascorrect);
                question.setText(vokab.get(i).relation[1]);
            }
        });
        c.gridy=2;
        panel.add(submit, c);
        frame.add(panel);
        frame.setVisible(true);
        return vokab;
    }
}
