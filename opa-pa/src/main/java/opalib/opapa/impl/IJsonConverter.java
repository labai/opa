package opalib.opapa.impl;

/**
 * @author Augustus
 * created on 2021.06.19
 */
public interface IJsonConverter {

    String getInputJson(Object opp);

    void applyResponseJson(Object opp, String json);

}
