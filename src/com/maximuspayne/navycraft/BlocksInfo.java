package com.maximuspayne.navycraft;

import org.bukkit.Material;

/**
 * BlocksInfo file
 * Defines information on all known blocks that MoveCraft uses for various purposes.
 * @author Joel (Yogoda)
 */
public class BlocksInfo {

	public static BlockInfo[] blocks = new BlockInfo[256];

	public static void loadBlocksInfo() {

		// name, isDataBlock, needsSupport, isComplexBlock, item dropped, amount dropped, is grass cover, cardinal directions
		blocks[0] = new BlockInfo(0,"air", false, false, false, -1, 0, false);
		blocks[1] = new BlockInfo(1,"smoothstone", false, false, false, 4, 1, false);
		blocks[2] = new BlockInfo(2,"grass", false, false, false, 3, 1, false);
		blocks[3] = new BlockInfo(3,"dirt", true, false, false, 3, 1, false);
		blocks[4] = new BlockInfo(4,"cobblestone", false, false, false, false);
		blocks[5] = new BlockInfo(5,"wood", true, false, false, false);
		blocks[6] = new BlockInfo(6,"sapling", true, false, false, false);
		blocks[7] = new BlockInfo(7,"adminium", false, false, false, false);
		blocks[8] = new BlockInfo(8,"water", false, false, false, -1, 0, false);
		blocks[9] = new BlockInfo(9,"water", false, false, false, -1, 0, false);
		blocks[10] = new BlockInfo(10,"lava", true, false, false, -1, 0, false);
		blocks[11] = new BlockInfo(11,"lava", true, false, false, -1, 0, false);
		blocks[12] = new BlockInfo(12,"sand", false, false, false, false);
		blocks[13] = new BlockInfo(13,"gravel", false, false, false, false);
		blocks[14] = new BlockInfo(14,"gold ore", false, false, false, false);
		blocks[15] = new BlockInfo(15,"iron ore", false, false, false, false);
		blocks[16] = new BlockInfo(16,"charcoal", false, false, false, 263, 1, false);
		blocks[17] = new BlockInfo(17,"trunk", true, false, false, false);
		blocks[18] = new BlockInfo(18,"leaves", true, false, false, -1, 0, false);
		blocks[19] = new BlockInfo(19,"sponge", true, false, false, false);
		blocks[20] = new BlockInfo(20,"glass", false, false, false, -1, 0, false);
		blocks[21] = new BlockInfo(21,"lapis ore", false, false, false, 21, 0, false);
		blocks[22] = new BlockInfo(22,"lapis block", false, false, false, 22, 0, false);
		blocks[23] = new BlockInfo(23,"dispenser", true, false, true, new byte[] {4, 2, 5, 3});
		blocks[24] = new BlockInfo(24,"sandstone", true, false, false, false);
		blocks[25] = new BlockInfo(25,"note", true, false, false, false);
		blocks[26] = new BlockInfo(26,"bed", true, true, false, 355, 1, false);
		blocks[27] = new BlockInfo(27,"power rail", true, true, false, false);
		blocks[28] = new BlockInfo(28,"detector rail", true, true, false, false);
		blocks[29] = new BlockInfo(29,"sticky piston", true, true, true, new byte[] {2, 5, 3, 4});
		blocks[30] = new BlockInfo(30,"web", false, false, false, false);
		blocks[31] = new BlockInfo(31,"tall grass", true, true, false, true);
		blocks[32] = new BlockInfo(32,"dead bush", true, true, false, true);
		blocks[33] = new BlockInfo(33,"piston", true, true, true, new byte[] {2, 5, 3, 4});
		blocks[34] = new BlockInfo(34,"piston head", false, false, false, new byte[] {2, 5, 3, 4});
		blocks[35] = new BlockInfo(35,"wool", true, false, false, 35, 1, false);
		blocks[36] = new BlockInfo(36,"piston extended", false, false, false, false);
		blocks[37] = new BlockInfo(37,"yellow flower", false, true, false, true);
		blocks[38] = new BlockInfo(38,"red flower", false, true, false, true);
		blocks[39] = new BlockInfo(39,"brown mushroom", false, true, false, true);
		blocks[40] = new BlockInfo(40,"red mushroom", false, true, false, true);
		blocks[41] = new BlockInfo(41,"gold block", false, false, false, 266, 9, false);
		blocks[42] = new BlockInfo(42,"iron block", false, false, false, 265, 9, false);
		blocks[43] = new BlockInfo(43,"double steps", true, false, false, 44, 2, false);
		blocks[44] = new BlockInfo(44,"step", true, false, false, false);
		blocks[45] = new BlockInfo(45,"brick", false, false, false, 336, 4, false);
		blocks[46] = new BlockInfo(46,"TNT", false, false, false, false);
		blocks[47] = new BlockInfo(47,"library", false, false, false, false);
		blocks[48] = new BlockInfo(48,"mossy cobblestone", false, false, false, false);
		blocks[49] = new BlockInfo(49,"obsidian", false, false, false, false);
		blocks[50] = new BlockInfo(50,"torch", true, true, false, false);
		blocks[51] = new BlockInfo(51,"fire", true, true, false, -1, 0, false);
		blocks[52] = new BlockInfo(52,"spawner", true, false, false, false);
		blocks[53] = new BlockInfo(53,"wooden stair", true, false, false, false);
		blocks[54] = new BlockInfo(54,"chest", true, false, true, false);
		blocks[55] = new BlockInfo(55,"redstone dust", true, true, false, 331, 1, false);
		blocks[56] = new BlockInfo(56,"diamond", false, false, false, 264, 1, false);
		blocks[57] = new BlockInfo(57,"diamond block", false, false, false, 264, 9, false);
		blocks[58] = new BlockInfo(58,"workbench", false, false, false, false);
		blocks[59] = new BlockInfo(59,"seed", true, true, false, 295, 1, false);
		blocks[60] = new BlockInfo(60,"field", true, false, false, 3, 1, false);
		blocks[61] = new BlockInfo(61,"furnace", false, true, 4, 8, new byte[] {4, 2, 5, 3}); /* Might need support...*/
		blocks[62] = new BlockInfo(62,"furnace", false, true, 4, 8, new byte[] {4, 2, 5, 3});
		blocks[63] = new BlockInfo(63,"sign", false, true, true, 323, 1, false);
		blocks[64] = new BlockInfo(64,"wooden door", true, true, false, 5, 3, false);
		blocks[65] = new BlockInfo(65,"ladder", true, true, false, false);
		blocks[66] = new BlockInfo(66,"rail", true, true, false, false);
		blocks[67] = new BlockInfo(67,"cobblestone stair", true, false, false, false);
		blocks[68] = new BlockInfo(68,"sign", false, true, true, 323, 1, false);
		blocks[69] = new BlockInfo(69,"lever", true, true, false, false);
		blocks[70] = new BlockInfo(70,"pressure plate", true, true, false, false);
		blocks[71] = new BlockInfo(71,"steel door", true, true, false, 265, 3, false);
		blocks[72] = new BlockInfo(72,"wooden pressure plate", true, true, false, false);
		blocks[73] = new BlockInfo(73,"redstone ore", false, false, false, 331, 4, false);
		blocks[74] = new BlockInfo(74,"redstone ore", false, false, false, 331, 4, false);
		blocks[75] = new BlockInfo(75,"redstone torch", true, true, false, false);
		blocks[76] = new BlockInfo(76,"redstone torch", true, true, false, false);
		blocks[77] = new BlockInfo(77,"stone button", true, true, false, false);
		blocks[78] = new BlockInfo(78,"snow", true, true, false, 332, 1, true);
		blocks[79] = new BlockInfo(79,"ice", false, false, false, false);
		blocks[80] = new BlockInfo(80,"snow block", false, false, false, false);
		blocks[81] = new BlockInfo(81,"cacti", true, true, false, false);
		blocks[82] = new BlockInfo(82,"clay", false, true, false, false);
		blocks[83] = new BlockInfo(83,"reed", true, true, false, 338, 1, false);
		blocks[84] = new BlockInfo(84,"jukebox", true, false, false, false);
		blocks[85] = new BlockInfo(85,"fence", false, false, false, false);
		blocks[86] = new BlockInfo(86,"pumpkin", true, false, false, new byte[] {3, 0, 1, 2});
		blocks[87] = new BlockInfo(87,"hellstone", false, false, false, false);
		blocks[88] = new BlockInfo(88,"mud", false, false, false, false);
		blocks[89] = new BlockInfo(89,"lightstone", false, false, false, false);
		blocks[90] = new BlockInfo(90,"portal", true, true, false, false);
		blocks[91] = new BlockInfo(91,"pumpkin", true, false, false, new byte[] {3, 0, 1, 2});
		blocks[92] = new BlockInfo(92,"cake", true, false, false, false);
		blocks[93] = new BlockInfo(93,"repeater", true, true, false, new byte[] {3, 0, 2, 1});
		blocks[94] = new BlockInfo(94,"repeater", true, true, false, new byte[] {3, 0, 2, 1});
		blocks[96] = new BlockInfo(96,"trapdoor", true, true, false, false);
		blocks[97] = new BlockInfo(97,"hidden silverfish", true, false, false, false);
		blocks[98] = new BlockInfo(98,"stone brick", true, false, false, false);
		blocks[99] = new BlockInfo(99,"huge brown mushroom", true, false, false, false);
		blocks[100] = new BlockInfo(100,"huge red mushroom", true, false, false, false);
		blocks[101] = new BlockInfo(101,"iron bars", false, false, false, false);
		blocks[102] = new BlockInfo(102,"glass pane", false, false, false, false);
		blocks[103] = new BlockInfo(103,"melon", false, false, false, false);
		blocks[104] = new BlockInfo(104, "pumpkin stem", true, false, false, false);
		blocks[105] = new BlockInfo(105, "melon stem", true, true, false, false);
		blocks[106] = new BlockInfo(106, "vines", true, true, false, false);
		blocks[107] = new BlockInfo(107,"fence gate", true, true, false, new byte[] {3, 0, 1, 2});
		blocks[108] = new BlockInfo(108, "brick stairs", true, false, false, false);
		blocks[109] = new BlockInfo(109, "stone brick stairs", true, false, false, false);
		blocks[110] = new BlockInfo(110, "mycelium", false, false, false, false);
		blocks[111] = new BlockInfo(111, "lilypad", false, true, false, false);
		blocks[112] = new BlockInfo(112, "netherbrick", false, false, false, false);
		blocks[113] = new BlockInfo(113, "nether brick fence", false, false, false, false);
		blocks[114] = new BlockInfo(114, "nether brick stairs", true, false, false, false);
		blocks[115] = new BlockInfo(115, "nether wart", true, true, false, false);
		blocks[116] = new BlockInfo(116,"enchantment table", true, false, true, false);
		blocks[117] = new BlockInfo(117,"brewing stand", true, true, true, false);
		blocks[118] = new BlockInfo(118,"cauldron", true, true, false, false);
		blocks[119] = new BlockInfo(119,"end portal", false, true, true, false);
		blocks[120] = new BlockInfo(120,"end portal frame", true, false, false, new byte[] {3, 0, 1, 2});
		blocks[121] = new BlockInfo(121,"end stone", false, false, false, false);
		blocks[122] = new BlockInfo(122,"dragon egg", false, true, false, false);
		blocks[123] = new BlockInfo(123,"redstone lamp off", false, false, false, false);
		blocks[124] = new BlockInfo(124,"redstone lamp on", false, false, false, false);
		blocks[125] = new BlockInfo(125,"wooden double slab", true, false, false, 126, 2, false);
		blocks[126] = new BlockInfo(126,"wooden slab", true, false, false, false);
		blocks[127] = new BlockInfo(127,"cocoa plant", true, true, false, false);
		blocks[128] = new BlockInfo(128, "sandstone stairs", true, false, false, false);
		blocks[129] = new BlockInfo(129,"emerald ore", false, false, false, false);
		blocks[130] = new BlockInfo(130,"ender chest", true, false, true, false);
		blocks[131] = new BlockInfo(131,"tripwire hook", true, true, true, false);
		blocks[132] = new BlockInfo(132,"tripwire", true, true, true, false);
		blocks[133] = new BlockInfo(133,"emerald block", false, false, false, false);
		blocks[134] = new BlockInfo(134,"spruce wood stairs", true, false, false, false);
		blocks[135] = new BlockInfo(135,"birch wood stairs", true, false, false, false);
		blocks[136] = new BlockInfo(136,"jungle wood stairs", true, false, false, false);
		blocks[137] = new BlockInfo(137,"command block", true, false, true, false);
		blocks[138] = new BlockInfo(138,"beacon block", true, false, true, false);
		blocks[139] = new BlockInfo(139,"cobblestone wall", true, false, false, false);
		blocks[140] = new BlockInfo(140,"flower pot", true, false, true, false);
		blocks[141] = new BlockInfo(141,"carrots", true, true, false, false);
		blocks[142] = new BlockInfo(142,"potatoes", true, true, false, false);
		blocks[143] = new BlockInfo(143,"wood button", true, true, false, false);
		blocks[144] = new BlockInfo(144,"head", true, true, false, false);
		blocks[145] = new BlockInfo(145,"anvil", true, true, false, false);
		blocks[146] = new BlockInfo(146,"trapped chest", true, false, true, false);
		blocks[147] = new BlockInfo(147,"weighted light pressure plate", true, true, false, false);
		blocks[148] = new BlockInfo(148,"weighted heavy pressure plate", true, true, false, false);
		blocks[149] = new BlockInfo(149,"comparator on", true, true, false, new byte[] {3, 0, 2, 1});
		blocks[150] = new BlockInfo(150,"comparator off", true, true, false, new byte[] {3, 0, 2, 1});
		blocks[151] = new BlockInfo(151,"daylight sensor", true, true, false, false);
		blocks[152] = new BlockInfo(152,"block of redstone", false, false, false, false);
		blocks[153] = new BlockInfo(153,"nether quartz ore", false, false, false, false);
		blocks[154] = new BlockInfo(154,"hopper", true, false, true, new byte[] {4, 2, 5, 3});
		blocks[155] = new BlockInfo(155,"block of quartz", true, false, false, false);
		blocks[156] = new BlockInfo(156,"quartz stairs", true, false, false, false);
		blocks[157] = new BlockInfo(157,"activator rail", true, true, false, false);
		blocks[158] = new BlockInfo(158,"dropper", true, false, true, new byte[] {4, 2, 5, 3});
		blocks[159] = new BlockInfo(159,"stained clay", true, false, false, false);
		blocks[170] = new BlockInfo(170,"hay bale", true, false, false, false);
		blocks[171] = new BlockInfo(171,"carpet", true, false, false, false);
		blocks[172] = new BlockInfo(172,"hardened clay", false, false, false, false);
		blocks[173] = new BlockInfo(173,"coal block", false, false, false, false);
		blocks[174] = new BlockInfo(174,"packed ice", false, false, false, false);
		blocks[175] = new BlockInfo(175,"flowers", false, true, false, true);
		blocks[176] = new BlockInfo(176,"free stand banner", true, true, false, false);
		blocks[177] = new BlockInfo(177,"wall banner", true, true, false, false);
		blocks[178] = new BlockInfo(178,"inverse daylight sensor", true, true, false, false);
		blocks[179] = new BlockInfo(179,"red sandstone", true, false, false, false);
		blocks[180] = new BlockInfo(180,"red sandstone stairs", true, false, false, false);
		blocks[181] = new BlockInfo(181,"double red sandstone slab", true, false, false, false);
		blocks[182] = new BlockInfo(182,"red sandstone slab", true, false, false, false);
		blocks[183] = new BlockInfo(183,"spruce fence gate", true, true, false, new byte[] {3, 0, 1, 2});
		blocks[184] = new BlockInfo(184,"birch fence gate", true, true, false, new byte[] {3, 0, 1, 2});
		blocks[185] = new BlockInfo(185,"jungle fence gate", true, true, false, new byte[] {3, 0, 1, 2});
		blocks[186] = new BlockInfo(186,"dark oakfence gate", true, true, false, new byte[] {3, 0, 1, 2});
		blocks[187] = new BlockInfo(187,"acacia fence gate", true, true, false, new byte[] {3, 0, 1, 2});
		blocks[188] = new BlockInfo(188,"spruce fence", false, false, false, false);
		blocks[189] = new BlockInfo(189,"birch fence", false, false, false, false);
		blocks[190] = new BlockInfo(190,"jungle fence", false, false, false, false);
		blocks[191] = new BlockInfo(191,"dark oak fence", false, false, false, false);
		blocks[192] = new BlockInfo(192,"acacia fence", false, false, false, false);
		blocks[193] = new BlockInfo(193,"spruce wooden door", true, true, false, 5, 3, false);
		blocks[194] = new BlockInfo(194,"birch wooden door", true, true, false, 5, 3, false);
		blocks[195] = new BlockInfo(195,"jungle wooden door", true, true, false, 5, 3, false);
		blocks[196] = new BlockInfo(196,"acacia wooden door", true, true, false, 5, 3, false);
		blocks[197] = new BlockInfo(197,"dark oak wooden door", true, true, false, 5, 3, false);
		blocks[198] = new BlockInfo(198,"end rod", false, false, false, false);
		blocks[199] = new BlockInfo(199,"chorus plant", false, false, false, false);
		blocks[200] = new BlockInfo(200,"chorus flower", false, false, false, false);
		blocks[201] = new BlockInfo(201,"purpur block", false, false, false, false);
		blocks[202] = new BlockInfo(202,"purpur pillar", false, false, false, false);
		blocks[203] = new BlockInfo(203,"purpur stairs", true, false, false, false);
		blocks[204] = new BlockInfo(204,"purpur double slabs", true, false, false, 205, 2, false);
		blocks[205] = new BlockInfo(205,"purpur slab", true, false, false, false);
		blocks[206] = new BlockInfo(206,"endstone bricks", false, false, false, false);
		blocks[207] = new BlockInfo(207,"beetroot", true, true, false, false);
		blocks[208] = new BlockInfo(208,"grass path", false, false, false, false);
		blocks[209] = new BlockInfo(209,"end gateway", true, true, false, false);
		blocks[210] = new BlockInfo(210,"repeating command block", true, false, true, false);
		blocks[211] = new BlockInfo(211,"chain command block", true, false, true, false);
		blocks[212] = new BlockInfo(212,"frosted ice", true, false, false, false);
		blocks[213] = new BlockInfo(213,"magma block", false, false, false, false);
		blocks[214] = new BlockInfo(214,"nether wart block", false, false, false, false);
		blocks[215] = new BlockInfo(215,"red nether brick", false, false, false, false);
		blocks[216] = new BlockInfo(216,"bone block", false, false, false, false);
		blocks[217] = new BlockInfo(217,"structure void", true, false, false, false);
		blocks[218] = new BlockInfo(218,"observer", true, false, false, false);
		blocks[219] = new BlockInfo(219,"white shulker", true, false, true, false);
		blocks[220] = new BlockInfo(220,"orange shulker", true, false, true, false);
		blocks[221] = new BlockInfo(221,"magenta shulker", true, false, true, false);
		blocks[222] = new BlockInfo(222,"light blue shulker", true, false, true, false);
		blocks[223] = new BlockInfo(223,"yellow shulker", true, false, true, false);
		blocks[224] = new BlockInfo(224,"lime shulker", true, false, true, false);
		blocks[225] = new BlockInfo(225,"pink shulker", true, false, true, false);
		blocks[226] = new BlockInfo(226,"gray shulker", true, false, true, false);
		blocks[227] = new BlockInfo(227,"light gray shulker", true, false, true, false);
		blocks[228] = new BlockInfo(228,"cyan shulker", true, false, true, false);
		blocks[229] = new BlockInfo(229,"purple shulker", true, false, true, false);
		blocks[230] = new BlockInfo(230,"blue shulker", true, false, true, false);
		blocks[231] = new BlockInfo(231,"brown shulker", true, false, true, false);
		blocks[232] = new BlockInfo(232,"green shulker", true, false, true, false);
		blocks[233] = new BlockInfo(233,"red shulker", true, false, true, false);
		blocks[234] = new BlockInfo(234,"black shulker", true, false, true, false);
		blocks[235] = new BlockInfo(235,"white glazed terracota", true, false, false, false);
		blocks[236] = new BlockInfo(236,"orange glazed terracota", true, false, false, false);
		blocks[237] = new BlockInfo(237,"magenta glazed terracota", true, false, false, false);
		blocks[238] = new BlockInfo(238,"lightblue glazed terracota", true, false, false, false);
		blocks[239] = new BlockInfo(239,"yellow glazed terracota", true, false, false, false);
		blocks[240] = new BlockInfo(240,"lime glazed terracota", true, false, false, false);
		blocks[241] = new BlockInfo(241,"pink glazed terracota", true, false, false, false);
		blocks[242] = new BlockInfo(242,"gray glazed terracota", true, false, false, false);
		blocks[243] = new BlockInfo(243,"lightgray glazed terracota", true, false, false, false);
		blocks[244] = new BlockInfo(244,"cyan glazed terracota", true, false, false, false);
		blocks[245] = new BlockInfo(245,"purple glazed terracota", true, false, false, false);
		blocks[246] = new BlockInfo(246,"blue glazed terracota", true, false, false, false);
		blocks[247] = new BlockInfo(247,"brown glazed terracota", true, false, false, false);
		blocks[248] = new BlockInfo(248,"green glazed terracota", true, false, false, false);
		blocks[249] = new BlockInfo(249,"red glazed terracota", true, false, false, false);
		blocks[250] = new BlockInfo(250,"black glazed terracota", true, false, false, false);
		blocks[251] = new BlockInfo(251,"concrete", true, false, false, false);
		blocks[252] = new BlockInfo(252,"concrete powder", true, false, false, false);
		blocks[255] = new BlockInfo(255,"structure block", true, false, true, false);
		
		//bed
		blocks[26].cardinalDirections = new byte[] {1, 2, 3, 0};
		//torch
		blocks[50].cardinalDirections = new byte[] {2, 4, 1, 3};
		
		
		//stairs
		blocks[53].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[67].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[108].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[109].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[114].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[128].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[134].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[135].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[136].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[156].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[180].cardinalDirections = new byte[] {1, 3, 0, 2};
		blocks[203].cardinalDirections = new byte[] {1, 3, 0, 2};
		
		//sign
		//blocks[63].cardinalDirections = new byte[] {4, 2, 5, 3};
		blocks[63].cardinalDirections = new byte[] {5, 3, 4, 2};
		//wooden door
		blocks[64].cardinalDirections = new byte[] {0, 1, 2, 3};
		//ladder
		blocks[65].cardinalDirections = new byte[] {4, 2, 5, 3};
		//blocks[65].cardinalDirections = new byte[] {5, 3, 4, 2};
		
		//wall sign
		//blocks[68].cardinalDirections = new byte[] {4, 2, 5, 3};
		blocks[68].cardinalDirections = new byte[] {5, 3, 4, 2};
		//lever
		//blocks[69].cardinalDirections = new byte[] {2, 4, 1, 3};
		blocks[69].cardinalDirections = new byte[] {1, 3, 2, 4};
		//steel door
		blocks[71].cardinalDirections = new byte[] {0, 1, 2, 3};
		//restone torch on
		blocks[75].cardinalDirections = new byte[] {2, 4, 1, 3};
		//restone torch off
		blocks[76].cardinalDirections = new byte[] {2, 4, 1, 3};	
		//button		
		blocks[77].cardinalDirections = new byte[] {1, 3, 2, 4};
		//blocks[77].cardinalDirections = new byte[] {4, 1, 3, 2};
		//blocks[77].cardinalDirections = new byte[] {3, 1, 4, 2};
		//repeater		
		blocks[93].cardinalDirections = new byte[] {2, 3, 0, 1};
		//repeater (on?)		
		blocks[94].cardinalDirections = new byte[] {2, 3, 0, 1};
		//comparator	
		blocks[149].cardinalDirections = new byte[] {2, 3, 0, 1};
		//comparator (on?)		
		blocks[150].cardinalDirections = new byte[] {2, 3, 0, 1};
		//trapdoor
		blocks[96].cardinalDirections = new byte[] {3, 1, 2, 0};
		//wall banner
		blocks[177].cardinalDirections = new byte[] {5, 3, 4, 2};
	}

	@SuppressWarnings("deprecation")
	public static String getName(int blockId) {
		return Material.getMaterial(blockId).name();
	}

	public static boolean isDataBlock(int blockId) {
		if(blockId != -1 && blocks[blockId] == null) {
			NavyCraft.instance.DebugMessage("blocks(" + blockId + " is null!", 0);
			return false;
		}

		return blockId != -1 && blocks[blockId].isDataBlock;
	}

	public static boolean isComplexBlock(int blockId) {
		if(blockId != -1 && blocks[blockId] == null)
			return false;

		// So far just a sign or a chest
		return blockId != -1 && blocks[blockId].isComplexBlock;
	}

	public static boolean needsSupport(int blockId) {		
		if(blockId == -1)
			return false;
		if(blocks[blockId] == null)
			return false;

		return blocks[blockId].needSupport;
	}

	public static boolean coversGrass(int blockId) {
		if(blockId != -1 && blocks[blockId] == null)
			return false;

		return blockId != -1 && blocks[blockId].isGrassCover;
	}

	public static int getDropItem(int blockId){
		if(blockId != -1 && blocks[blockId] == null)
			return -1;

		return blocks[blockId].dropItem;
	}

	public static int getDropQuantity(int blockId){
		if(blockId != -1 && blocks[blockId] == null)
			return 0;

		return blocks[blockId].dropQuantity;
	}
	
	public static int getCardinalDirectionFromData(int BlockId, short BlockData) {
		if(blocks[BlockId].cardinalDirections == null) {
			System.out.println("Tried to get cardinals for " + BlockId + ", which has no cardinals.");
			return -1;
		}
		
		for (int i = 0; i < blocks[BlockId].cardinalDirections.length; i++) {
			if(BlockData == blocks[BlockId].cardinalDirections[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public static String getCardinalDirection(int BlockId, short BlockData) {
		if(blocks[BlockId].cardinalDirections == null)
			return "Woops";
			
		switch(getCardinalDirectionFromData(BlockId, BlockData)) {
		case 0:
			return "North";
		case 1:
			return "East";
		case 2:
			return "West";
		case 3:
			return "South";
		}
		
		return "";
	}
	
	public static byte[] getCardinals(int BlockId) {
		if(blocks[BlockId] == null) {
			System.out.println("NO BLOCK INFO FOUND FOR " + BlockId + "! PANIC!");
			return null;
		}
		
		byte[] returnVal = blocks[BlockId].cardinalDirections; 
		
		if (blocks[BlockId].cardinalDirections == null)
			return null;
		else
			return returnVal;
	}

	public static class BlockInfo {
		int id;
		boolean isDataBlock;
		boolean needSupport;
		boolean isComplexBlock;
		int     dropItem = -1;
		int     dropQuantity = 0;
		boolean isGrassCover; 
		private byte[] cardinalDirections = null;
		//by default, cardinals are usually 4,2,5,3 -> North,East,South,West

		/* Given grasscover */
		private BlockInfo(int id, String name, boolean isDataBlock, boolean needSupport, boolean isComplexBlock, boolean isGrassCover) {
			this(id, name, isDataBlock, needSupport, isComplexBlock, id, 1, isGrassCover);
		}

		/* Given cardinals */
		private BlockInfo(int id, String name, boolean isDataBlock, boolean needSupport, boolean isComplexBlock, byte[] cardinals) {
			this(id, name, isDataBlock, needSupport, isComplexBlock, id, 1, false);
			this.cardinalDirections = cardinals;
			//blocks[id].cardinalDirections = cardinals;
		}

		/* Given cardinals and dropItems */
		//cardinals are North, East, West, South
		private BlockInfo(int id, String name, boolean isDataBlock, boolean isComplexBlock, int dropItem, int dropQuantity, byte[] cardinals) {
			//this(id, name, isDataBlock, false, isComplexBlock, id, 1, false);
			this.id = id;
			this.isDataBlock = isDataBlock;
			this.isComplexBlock = isComplexBlock;
			this.dropItem = dropItem;
			this.dropQuantity = dropQuantity;
			this.cardinalDirections = cardinals;
			//blocks[id].cardinalDirections = cardinals;
		}

		/* Given dropitems */
		private BlockInfo(int id, String name, boolean isDataBlock, boolean needSupport,
				boolean isComplexBlock, int dropItem, int dropQuantity, boolean isGrassCover) {

			this.id = id;
			this.isDataBlock = isDataBlock;
			this.needSupport = needSupport;
			this.isComplexBlock = isComplexBlock;
			this.dropItem = dropItem;
			this.dropQuantity = dropQuantity;
			this.isGrassCover = isGrassCover;
		}

	}
}