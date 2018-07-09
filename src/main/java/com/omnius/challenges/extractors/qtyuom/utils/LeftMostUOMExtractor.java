package com.omnius.challenges.extractors.qtyuom.utils;

import com.omnius.challenges.extractors.qtyuom.QtyUomExtractor;
import com.omnius.challenges.extractors.qtyuom.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Implements {@link QtyUomExtractor} identifying as <strong>the most relevant UOM</strong> the leftmost UOM found in the articleDescription.
 * The {link UOM} array contains the list of valid UOMs. The algorithm search for the leftmost occurence of UOM[i], if there are no occurrences then tries UOM[i+1].
 * 
 * Example
 * <ul>
 * <li>article description: "black steel bar 35 mm 77 stck"</li>
 * <li>QTY: "77" (and NOT "35")</li>
 * <li>UOM: "stck" (and not "mm" since "stck" has an higher priority as UOM )</li>
 * </ul>
 *
 * @author <a href="mailto:damiano@searchink.com">Damiano Giampaoli</a>
 * @since 4 May 2018
 */
public class LeftMostUOMExtractor implements QtyUomExtractor {
    /**
     * Array of valid UOM to match. the elements with lower index in the array has higher priority
     */
    public static String[] UOM = {"stk", "stk.", "stck", "stück", "stg", "stg.", "st", "st.", "stange", "stange(n)", "tafel", "tfl", "taf", "mtr", "meter", "qm", "kg", "lfm", "mm", "m"};
    
    public LeftMostUOMExtractor() {}
    
    @Override
    public Pair<String, String> extract(String articleDescription) {
        //mock implementation
        if(articleDescription == null || articleDescription.trim().isEmpty()){
            return null;
        }

        Integer[] indexArray = findIndexOfArrays(articleDescription);
        Integer indexOfElementOfArrayArticle = indexArray[0];
        Integer indexOfElementOfUOM = indexArray[1];
        if(indexOfElementOfArrayArticle != null && indexOfElementOfUOM != null) {
            String qty = calculateQtyByGivenArticleByUOMIndex(articleDescription, indexOfElementOfArrayArticle);
            return new Pair<String, String>(qty,UOM[indexOfElementOfUOM]);
        }else{
            return null;
        }

    }

    @Override
    public Pair<Double, String> extractAsDouble(String articleDescription) {
        //mock implementation
        if(articleDescription == null || articleDescription.trim().isEmpty()){
            return null;
        }

        Integer[] indexArray = findIndexOfArrays(articleDescription);

        Integer indexOfElementOfArrayArticle = indexArray[0];
        Integer indexOfElementOfUOM = indexArray[1];

        if(indexOfElementOfArrayArticle != null && indexOfElementOfUOM != null) {
            String qty = calculateQtyByGivenArticleByUOMIndex(articleDescription, indexOfElementOfArrayArticle);
            Double dobuleQty = Double.valueOf(qty);

            return new Pair<Double, String>(dobuleQty,UOM[indexOfElementOfUOM]);
        }else{
            return null;
        }
    }

    private Integer[] findIndexOfArrays(String article){
        Integer[] responseArray = new Integer[2];
        Integer articleArrayIndex = null;
        Integer uomArrayIndex = null;

        List<String> listOfUom = Arrays.asList(UOM);
        String[] articleElements = article.split(" ");

        for(int i=0; i < articleElements.length; i++){
            if(listOfUom.contains(articleElements[i].toLowerCase())){
                if(uomArrayIndex == null || listOfUom.indexOf(articleElements[i].toLowerCase()) < uomArrayIndex) {
                    articleArrayIndex = i;
                    uomArrayIndex = listOfUom.indexOf(articleElements[i].toLowerCase());
                }
            }
        }
        responseArray[0] = articleArrayIndex;
        responseArray[1] = uomArrayIndex;

        return responseArray;
    }

    private String calculateQtyByGivenArticleByUOMIndex(String article, Integer indexOfElementOfArrayArticle){
        Stack<String> stringStack = new Stack<String>();
        String[] articleElements = article.split(" ");

        for(int i=indexOfElementOfArrayArticle-1; i >= 0; i--){
            if(articleElements[i].contains(",") && articleElements[i].length() > 1){
                if(isValidTextForQty(articleElements[i], ",")){
                    stringStack.add(articleElements[i]);
                }else{
                    break;
                }

            } else if(articleElements[i].contains(".") && articleElements[i].length() > 1){
                if(isValidTextForQty(articleElements[i], ".")){
                    stringStack.add(articleElements[i]);
                }else{
                    break;
                }
            } else {
                if(articleElements[i].equals(".") || articleElements[i].equals(",")){
                    continue;
                } else if(IsValidTextForConvert(articleElements[i])){
                    if(articleElements[i+1].equals(".") || articleElements[i+1].equals(",")){
                        stringStack.add(articleElements[i+1]);
                    }
                    stringStack.add(articleElements[i]);
                }else{
                    break;
                }
            }
        }

        String qty = "";
        while(!stringStack.empty())
        {
            qty += stringStack.pop();
        }

        return qty;
    }

    private boolean isValidTextForQty(String articleElement, String symbol) {

        Boolean validText = true;
        String[] arrayByComma = articleElement.split(symbol);

        for(int j=0; j<arrayByComma.length; j++){
            if(!IsValidTextForConvert(arrayByComma[j])){
                validText = false;
                break;
            }
        }

        return validText;
    }

    private Boolean IsValidTextForConvert(String text){
        try{
            Double convertedValue = Double.valueOf(text);
            if(text.length() > 3){
                if(convertedValue%100 != 0){
                    return false;
                }
            }

            return true;
        }catch(Exception ex){

            return false;
        }
    }

}
