package cn.jerio.exception;

import cn.jerio.result.CodeMsg;

/**
 * Created by Jerio on 2019/03/01
 */
public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 213432543653454354L;

    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
