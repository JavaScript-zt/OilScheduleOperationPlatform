package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.problem.onlinemix.Oilschdule;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ����
 */
public class OnlineMix_MOEAs_test {

    public static void main(String[] args) {

        String rst = "0.006030095115012426 0.010699011552536195 0.03736560804713082 0.033643034791446653 0.009007874138196498 0.00561452068692161 0.16121561787077354 8.939835147213556E-4 0.0019546549225712027 0.0019294685662316312 0.0014316198308920397 0.027373930499365404 0.023932284462424167 0.004574731440723381 7.130666772606391E-4 0.0023754858874102535 0.020629739981874617 0.0511475662940484 0.026015321533278454 0.0033333230458208823 0.005457947888745088 0.01326050628213353 0.3015038802202548 0.2554179487472009 0.00515554667817213 8.552929034256724E-4 0.06991886414191817 0.2751486158959071 0.03136228243207831 0.004944603776488539 0.002143714159731406 0.7535448152284953 0.32400217491335015 0.7726618018815113 0.004646005643894716 0.008405089768229427 0.09626816098404546 0.21633881567389468 0.029656691681445843 0.005082855347310909 0.0018553115909460444 0.38991924407898293 0.011237633886478991 0.015885502017481807 0.004373113237514781 1.9885858046207776E-5 0.02563896097106111 0.003636427545021531 0.0012042664583655684 0.0060197295896614895 0.0020539572927474664 0.0030714053998913077 6.673106080557135E-4 0.0030208787921077716 0.0011653601221364353 0.006802659417175624 0.2404673910879963 0.1507336214651791 0.6713587665214155 8.406672067167156E-4 0.00839985280784565 0.005740855898308447 0.005682580805325457 0.005210079342830755 0.0016728674299399209 0.00561422902524405 0.004721977770137924 0.002620349307816369 0.00579930774339972 4.813328481587984E-4 0.0012610849441650946 0.007782995708776161 0.0076674671319656535 0.004054910542589586 0.004136174598984143 0.005115395914923343 0.004709941962908076 8.29881865917521E-5 0.0030666506440231098 0.004500827135027683 0.0016349625264373427 0.009156393609075228 0.002152789054289469 0.0019332264288970445 5.101055669229559E-4 7.183833613500779E-4 0.002217919316199795 0.004574156940228645 0.0029190991819302674 0.0018786495754218632 0.0025753258169574017 0.005482309952652816 1.7362140194461707E-4 0.0019621417867916823 0.002107518239596094 0.0015685163715088764 0.0024823893208753794 0.0028478661035536076 0.004349562675484969 0.0027755626037259196 0.002630928865644597 0.005415706241927333 0.006050066605264331 2.1735243609358093E-4 0.003379620674858427 0.005844491103590863 0.003558778050913132 0.004156071088086614 0.004133521568369198 0.003583871328730875 0.00400730277872076 0.004709577420906347 0.004533897741121662 9.812636499418478E-4 0.0064028232624951786 5.547155421522636E-4 0.0028437626798411843 0.008943350079072378 0.0012210039095967877 0.005880462494426605 0.00170200097654306 0.007246975081785917 0.0013576753347651325 0.006512980178744649 0.004318466237049458";

        String[] strings = rst.split(" ");
        List<String> strList = Arrays.asList(strings);
        List<Double> doubleList = strList.stream().mapToDouble(str -> Double.parseDouble(str)).boxed().collect(Collectors.toList());
        double[][] x = new double[1][doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            x[0][i] = doubleList.get(i);
        }
        Oilschdule.fat(x, true);
    }
}
