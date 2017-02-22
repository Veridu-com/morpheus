/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
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
     * @param factName name of the fact
     * @param providerName provider name for the fact
     * @param providerList provider list on which to compute the fact
     * @param numericMatch whether we want to match with a number or a string
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
     * @param factName name of the fact
     * @param providerName name of the provider
     * @param providerList provider list to compute the fact for
     * @param numericMatch whether we want to match with a number or a string
     * @param levenshteinThreshold threshold to run levenshtei. Use 0 for exact match.
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

    /**
     * Get the facts list
     *
     * @return facts list
     */
    public ArrayList<IFact> getFactList() {
        return factList;
    }

    /**
     * Generate the rules dynamically based on the options passed in the
     * constructor.
     */
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

    /**
     * Apply all rules
     *
     * @param factValues values of the facts on which to apply the rules
     * @return an array of 0 or 1 in case a rule passed.
     */
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

    /**
     * Apply a text equality rule
     *
     * @param results array on which to write
     * @param i position of feature
     * @param opCode operation code
     * @param val1 first value
     * @param val2 second value
     */
    private void applyTextEqualityRule(double[] results, int i, String opCode, String val1, String val2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPosWithTextEqualityTest(results, i, val1, val2);
            break;
        }
    }

    /**
     * Set a binary value with text equality test
     *
     * @param results array on which to write
     * @param pos position
     * @param val1 first string value
     * @param val2 second string value
     */
    private void setBinaryValueAtPosWithTextEqualityTest(double[] results, final int pos, String val1, String val2) {
        if ((val1 == null) || (val2 == null) || val1.equals("") || val2.equals(""))
            results[pos] = Utils.missingValue();
        else if (val1.equals(val2))
            results[pos] = 1;
        else
            results[pos] = 0;
    }

    /**
     * Set a binary value with numeric equality test
     *
     * @param results array on which to write
     * @param pos position
     * @param val1 first numeric value
     * @param val2 second numeric value
     */
    private void setBinaryValueAtPos(double[] results, final int pos, double val1, double val2) {
        if ((val1 <= 0) || (val2 <= 0))
            results[pos] = Utils.missingValue();
        else if (val1 == val2)
            results[pos] = 1;
        else
            results[pos] = 0;
    }

    /**
     * Apply a numeric rule
     *
     * @param results array on which to write
     * @param i position
     * @param opCode operation code
     * @param val1 first numeric value
     * @param val2 second numeric value
     */
    private void applyNumericRule(double[] results, int i, String opCode, double val1, double val2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPos(results, i, val1, val2);
            break;
        }
    }

    /**
     * Set a binary value at position
     *
     * @param results array on which to write
     * @param pos position
     * @param val1 first string value
     * @param val2 second string value
     */
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

    /**
     * Apply text similarity rule
     *
     * @param results array on which to write
     * @param i position
     * @param opCode operation code
     * @param s1 first string value
     * @param s2 second string value
     */
    public void applyTextSimilarityRule(double[] results, int i, String opCode, String s1, String s2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPos(results, i, s1, s2);
            break;
        }
    }

}
