package com.veridu.morpheus.utils;

import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.facts.IFact;
import weka.core.Utils;

import java.util.ArrayList;

public class RulesEngine {

    private double levenshteinThreshold = -1;
    private String factName;
    private String providerName;

    // we store the following so we can execute the rules faster than doing a lot of string parsing
    private ArrayList<Integer> ruleOperand1 = new ArrayList<>(); // index of the first provider of a rule
    private ArrayList<Integer> ruleOperand2 = new ArrayList<>(); // index of the second provider of a rule
    private ArrayList<String> opCodes = new ArrayList<>(); // operator of the rule, e.g., =, <, >, <=, >=
    private String[] providerList;

    private ArrayList<IFact> factList = new ArrayList<>();

    private boolean numericMatch;

    /**
     * Create a rules engine for numeric tests
     *
     * @param factName
     * @param providerName
     * @param providerList
     * @param numericMatch
     */
    public RulesEngine(String factName, String providerName, String[] providerList, boolean numericMatch) {
        this.factName = factName;
        this.providerName = providerName;
        this.numericMatch = numericMatch;
        this.providerList = providerList;

        generateDynamicRules();
    }

    /**
     * Create a rules engine instance for text tests
     *
     * @param factName
     * @param providerName
     * @param providerList
     * @param numericMatch
     * @param levenshteinThreshold
     *            Use 0 for exact match.
     */
    public RulesEngine(String factName, String providerName, String[] providerList, boolean numericMatch,
            double levenshteinThreshold) {
        this.factName = factName;
        this.providerName = providerName;
        this.levenshteinThreshold = levenshteinThreshold;
        this.numericMatch = numericMatch;
        this.providerList = providerList;

        generateDynamicRules();
    }

    public ArrayList<IFact> getFactList() {
        return factList;
    }

    private void generateDynamicRules() {
        for (int i = 0; i < (providerList.length - 1); i++)
            for (int j = i + 1; j < providerList.length; j++) {
                IFact fct = new Fact("is" + providerList[i] + this.factName + "Matches" + providerList[j],
                        this.providerName);
                this.factList.add(fct);
                ruleOperand1.add(i);
                ruleOperand2.add(j);
                opCodes.add("=");
            }
    }

    public double[] applyRules(ArrayList<String> factValues) {

        double[] results = new double[this.factList.size()];

        if (this.numericMatch)
            for (int i = 0; i < this.factList.size(); i++) {
                int op1 = ruleOperand1.get(i);
                int op2 = ruleOperand2.get(i);
                String opCode = opCodes.get(i);
                double val1 = Double.parseDouble(factValues.get(op1));
                double val2 = Double.parseDouble(factValues.get(op2));
                applyNumericRule(results, i, opCode, val1, val2);
            }
        else if (levenshteinThreshold > 0)
            // this is a text similarity search
            for (int i = 0; i < this.factList.size(); i++) {
                int op1 = ruleOperand1.get(i);
                int op2 = ruleOperand2.get(i);
                String opCode = opCodes.get(i);
                String val1 = factValues.get(op1);
                String val2 = factValues.get(op2);
                this.applyTextSimilarityRule(results, i, opCode, val1, val2);
            }
        else // exact text match
            for (int i = 0; i < this.factList.size(); i++) {
                int op1 = ruleOperand1.get(i);
                int op2 = ruleOperand2.get(i);
                String opCode = opCodes.get(i);
                String val1 = factValues.get(op1);
                String val2 = factValues.get(op2);
                this.applyTextEqualityRule(results, i, opCode, val1, val2);
            }
        return results;
    }

    private void applyTextEqualityRule(double[] results, int i, String opCode, String val1, String val2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPosWithTextEqualityTest(results, i, val1, val2);
            break;
        }
    }

    private void setBinaryValueAtPosWithTextEqualityTest(double[] results, final int pos, String val1, String val2) {
        if ((val1 == null) || (val2 == null) || val1.equals("") || val2.equals(""))
            results[pos] = Utils.missingValue();
        else if (val1.equals(val2))
            results[pos] = 1;
        else
            results[pos] = 0;
    }

    private void setBinaryValueAtPos(double[] results, final int pos, double val1, double val2) {
        if ((val1 <= 0) || (val2 <= 0))
            results[pos] = Utils.missingValue();
        else if (val1 == val2)
            results[pos] = 1;
        else
            results[pos] = 0;
    }

    private void applyNumericRule(double[] results, int i, String opCode, double val1, double val2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPos(results, i, val1, val2);
            break;
        }
    }

    private void setBinaryValueAtPos(double[] results, int pos, String val1, String val2) {
        if ((val1.equals("")) || (val2.equals(""))) {
            results[pos] = Utils.missingValue();
            return;
        }

        double sim = LocalUtils.normalizedLevenshteinSimilarity(val1, val2);

        if (sim >= this.levenshteinThreshold)
            results[pos] = 1;
        else
            results[pos] = 0;
    }

    public void applyTextSimilarityRule(double[] results, int i, String opCode, String s1, String s2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPos(results, i, s1, s2);
            break;
        }
    }

}
