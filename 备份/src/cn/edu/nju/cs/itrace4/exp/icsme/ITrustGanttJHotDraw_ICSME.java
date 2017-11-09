package cn.edu.nju.cs.itrace4.exp.icsme;

import cn.edu.nju.cs.itrace4.exp.gantt.icsme.Gantt_JS_ICSME;
import cn.edu.nju.cs.itrace4.exp.gantt.icsme.Gantt_LSI_ICSME;
import cn.edu.nju.cs.itrace4.exp.gantt.icsme.Gantt_VSM_ICSME;
import cn.edu.nju.cs.itrace4.exp.itrust.icsme.iTrust_JS_ICSME;
import cn.edu.nju.cs.itrace4.exp.itrust.icsme.iTrust_LSI_ICSME;
import cn.edu.nju.cs.itrace4.exp.itrust.icsme.iTrust_VSM_ICSME;
import cn.edu.nju.cs.itrace4.exp.jhotdraw.icsme.JHotDraw_JS_ICSME;
import cn.edu.nju.cs.itrace4.exp.jhotdraw.icsme.JHotDraw_LSI_ICSME;
import cn.edu.nju.cs.itrace4.exp.jhotdraw.icsme.JHotDraw_VSM_ICSME;

import java.io.IOException;

/**
 * Created by niejia on 16/3/30.
 */
public class ITrustGanttJHotDraw_ICSME {


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        iTrust_VSM_ICSME.run();
        iTrust_JS_ICSME.run();
        iTrust_LSI_ICSME.run();

        Gantt_VSM_ICSME.run();
        Gantt_JS_ICSME.run();
        Gantt_LSI_ICSME.run();

        JHotDraw_VSM_ICSME.run();
        JHotDraw_JS_ICSME.run();
        JHotDraw_LSI_ICSME.run();
    }
}
