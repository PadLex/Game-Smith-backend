package GUI;

import FileManager.Csv_handler;
import MatrixFactorization.MakeReccomendation;
import MatrixFactorization.MatrixUtility;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

public class ReccomenderStarter extends JFrame implements ActionListener {
    String logo_filepath = "Recommender/resources/LUDII_Icon_transparent.png";
    User_Rec curUser = new User_Rec();
    JRadioButton [] options = new JRadioButton[5];
    int current = 0;
    JLabel zero_label = new JLabel("0");
    ButtonGroup bg = new ButtonGroup();
    JLabel four_label = new JLabel("4");
    JButton next_button = new JButton("Next");
    JLabel question = new JLabel("From 0 to 4, how much do you enjoy games playing with multiple types of pieces?");
    String [] questionStrings = new String [] {"From 0 to 4, how much do you enjoy games playing with multiple types of pieces?", "From 0 to 4, how much do you like Chess?",
    "From 0 to 4, how much do you enjoy games playing with one type of piece?", "From 0 to 4, how much do you like Checkers/Draughts?", "From 0 to 4, how much do you enjoy games with the Mancala board?",
    "From 0 to 4, how much do you enjoy Puzzles?", "From 0 to 4, how much do you like Sudoku?", "From 0 to 4, how much do you enjoy games with an elements of chance?",
    "From 0 to 4, how much do enjoy games with hidden elements?", "From 0 to 4, how much do you enjoy Backgammon?", "From 0 to 4, how much do you enjoy games with two or more players?",
            "From 0 to 4, in games, how much do you enjoy cooperating with others?"};
    public ReccomenderStarter(){
        ImageIcon logo = new ImageIcon(logo_filepath);
        setIconImage(logo.getImage());
        int count = 0;
        question.setBounds(12, 100, 900, 50);
        question.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        question.setFont(new Font("Arial", Font.BOLD, 16));
        add(question);
        int idx = 0;
        for (JRadioButton button : options){
            button = new JRadioButton();
            button.setBounds(285 + count, 170, 15, 15);
            button.setBackground(Color.LIGHT_GRAY);
            button.setVisible(true);
            bg.add(button);
            add(button);
            count+=20;
            this.options[idx] = button;
            idx++;
        }
        options[2].setSelected(true);
        zero_label.setBounds(260, 170, 15, 15);
        four_label.setBounds(405, 170, 15, 15);
        next_button.setBounds(270, 200, 120, 40);
        next_button.addActionListener(this);
        next_button.setBackground(Color.CYAN);
        next_button.setForeground(Color.darkGray);
        next_button.setFont(new Font("Arial Black", Font.BOLD, 15));
        add(next_button);
        add(zero_label);
        add(four_label);
        setSize(650, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        setLayout(null);
        setVisible(true);
        setResizable(false);
    }

    public static void main(String[] args) {
        new ReccomenderStarter();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        switch(current){
            case 0:
                current++;
                final float average_multiple_pieces_rating = 7.159033260533334f;
                question.setText(questionStrings[current]);
                int idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                float change;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_multiple_pieces_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_multiple_pieces_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_multiple_pieces_ratings(average_multiple_pieces_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 1:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }

                curUser.set_chess_rating(idx/4f * 10f);
                current++;
                options[2].setSelected(true);
                break;
            case 2:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_one_piece_rating = 6.110978055755186f;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_one_piece_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_one_piece_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_one_piece_ratings(average_one_piece_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 3:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                curUser.set_checkers_rating(idx/4f * 10f);
                current++;
                options[2].setSelected(true);
                break;
            case 4:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_mancala_rating = 5.994619039587398f;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_mancala_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_mancala_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_mancala_ratings(average_mancala_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 5:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_puzzle_rating = 6.217805628742515f;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_puzzle_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_puzzle_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_mancala_ratings(average_puzzle_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 6:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                curUser.set_sudoku_rating(idx/4f * 10f);
                current++;
                options[2].setSelected(true);
                break;
            case 7:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_chance_rating = 6.466419787010439f;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_chance_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_chance_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_mancala_ratings(average_chance_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 8:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_hidden_element_rating = 5.7176979303231725f;
                change = 0;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_hidden_element_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_hidden_element_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_mancala_ratings(average_hidden_element_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 9:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                curUser.set_backgammon_rating(idx/4f * 10f);
                current++;
                options[2].setSelected(true);
                break;
            case 10:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_multiplayer_rating = 5.7176979303231725f;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_multiplayer_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_multiplayer_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_mancala_ratings(average_multiplayer_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 11:
                question.setText(questionStrings[current]);
                idx = 0;
                for (int i=0; i<options.length; i++){
                    if(options[i].isSelected()){
                        idx = i;
                    }
                }
                final float average_coordination_rating = 4.5411965294793255f;
                idx -= 2;
                if (idx > 0){
                    float max = (10f-average_coordination_rating)/2f;
                    change = max * idx/2f;
                }
                else {
                    float min = (average_coordination_rating-0f)/2f;
                    change = min * idx/2f;
                }
                curUser.set_mancala_ratings(average_coordination_rating + change);
                current++;
                options[2].setSelected(true);
                break;
            case 12:
                float [][] u_r_m = Csv_handler.parse_csv_to_matrix_2("resources/MF Results/first_use_lowest.csv");
                float [][] u = Csv_handler.parse_csv_to_matrix_2("resources/MF Results/first_use_u_matrix_final.csv");
                float [][] q = MatrixUtility.transpose(Csv_handler.parse_csv_to_matrix_2("resources/MF Results/first_use_q_matrix_final.csv"));
                MakeReccomendation mr = new MakeReccomendation(curUser.rating_vector, u_r_m, u, q);
                mr.update_recs();
                mr.user_n_most_liked_games(3);
                String desc_format = mr.fav_game_desc();
                System.out.println(desc_format);
                break;

        }
    }
}
