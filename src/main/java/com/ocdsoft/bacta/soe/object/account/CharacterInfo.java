package com.ocdsoft.bacta.soe.object.account;

import lombok.Data;

/**
 * 
060E51D5   -   human male		
04FEC8FA   -   trandoshan male	
32F6307A   -   twilek male		
9B81AD32   -   bothan male		
22727757   -   zabrak male		
CB8F1F9D   -   rodian male		
79BE87A9   -   moncal male		
2E3CE884   -   wookiee male		
1C95F5BC   -   sullstan male		
D3432345   -   ithorian male		
D4A72A70   -   human female		
64C24976   -   trandoshan female	
6F6EB65D   -   twilek female		
F6AB978F   -   bothan female		
421ABB7C   -   zabrak female		
299DC0DA   -   rodian female		
73D65B5F   -   moncal female		
1AAD09FA   -   wookiee female	
44739CC1   -   sullstan female	
E7DA1366   -   ithorian female */

@Data
public class CharacterInfo {
    public static final int CT_normal = 0x1;
    public static final int CT_jedi = 0x2;
    public static final int CT_spectral = 0x3;

	private String name; //UnicodeString
    private int objectTemplateId;
    private long networkId; //NetworkId
    private int clusterId;
    private int characterType;
    private boolean disabled;
}