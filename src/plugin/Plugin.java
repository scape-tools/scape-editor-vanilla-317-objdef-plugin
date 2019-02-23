package plugin;

import scape.editor.fs.io.RSBuffer;
import scape.editor.gui.plugin.PluginDescriptor;
import scape.editor.gui.plugin.extension.config.ConfigExtension;

@PluginDescriptor(name="Vanilla 317 Object Definition Plugin", authors = "Nshusa", version = "2.0.0")
public class Plugin extends ConfigExtension {

    @Override
    public String applicationIcon() {
        return "icons/icon.png";
    }

    @Override
    public String fxml() {
        return "scene.fxml";
    }

    @Override
    public String[] stylesheets() {
        return new String[]{
                "css/style.css"
        };
    }

    @Override
    public String getFileName() {
        return "loc";
    }

    @Override
    protected void decode(int currentIndex, RSBuffer buffer) {
        int interactive = -1;
        id = currentIndex;

        while(true) {
            int opcode = buffer.readUByte();

            if (opcode == 0) {
                break;
            }

            if (opcode == 1) {
                int count = buffer.readUByte();
                if (count > 0) {
                    if (modelIds == null) {
                        modelTypes = new int[count];
                        modelIds = new int[count];

                        for (int i = 0; i < count; i++) {
                            modelIds[i] = buffer.readUShort();
                            modelTypes[i] = buffer.readUByte();
                        }
                    } else {
                        buffer.setPosition(buffer.getPosition() + (count * 3));
                    }
                }
            } else if (opcode == 2) {
                name = buffer.readString10();
            } else if (opcode == 3) {
                description = buffer.readString10();
            } else if (opcode == 5) {
                int count = buffer.readUByte();
                if (count > 0) {
                    if (modelIds == null) {
                        modelTypes = null;
                        modelIds = new int[count];

                        for (int i = 0; i < count; i++) {
                            modelIds[i] = buffer.readUShort();
                        }
                    } else {
                        buffer.setPosition(buffer.getPosition() + (count * 2));
                    }
                }
            } else if (opcode == 14) {
                width = buffer.readUByte();
            } else if (opcode == 15) {
                length = buffer.readUByte();
            } else if (opcode == 17) {
                solid = false;
            } else if (opcode == 18) {
                impenetrable = false;
            } else if (opcode == 19) {
                interactive = buffer.readUByte();
                if (interactive == 1) {
                    this.interactive = true;
                }
            } else if (opcode == 21) {
                contouredGround = true;
            } else if (opcode == 22) {
                delayShading = true;
            } else if (opcode == 23) {
                occludes = true;
            } else if (opcode == 24) {
                animation = buffer.readUShort();
                if (animation == 65535) {
                    animation = -1;
                }
            } else if (opcode == 28) {
                decorDisplacement = buffer.readUByte();
            } else if (opcode == 29) {
                ambientLighting = buffer.readByte();
            } else if (opcode == 39) {
                lightDiffusion = buffer.readByte();
            } else if (opcode >= 30 && opcode < 39) {
                if (interactions == null) {
                    interactions = new String[5];
                }
                interactions[opcode - 30] = buffer.readString10();
                if (interactions[opcode - 30].equalsIgnoreCase("hidden")) {
                    interactions[opcode - 30] = null;
                }
            } else if (opcode == 40) {
                int count = buffer.readUByte();
                originalColours = new int[count];
                replacementColours = new int[count];
                for (int i = 0; i < count; i++) {
                    originalColours[i] = buffer.readUShort();
                    replacementColours[i] = buffer.readUShort();
                }

            } else if (opcode == 60) {
                minimapFunction = buffer.readUShort();
            } else if (opcode == 62) {
                inverted = true;
            } else if (opcode == 64) {
                castsShadow = false;
            } else if (opcode == 65) {
                scaleX = buffer.readUShort();
            } else if (opcode == 66) {
                scaleY = buffer.readUShort();
            } else if (opcode == 67) {
                scaleZ = buffer.readUShort();
            } else if (opcode == 68) {
                mapscene = buffer.readUShort();
            } else if (opcode == 69) {
                surroundings = buffer.readUByte();
            } else if (opcode == 70) {
                translateX = buffer.readShort();
            } else if (opcode == 71) {
                translateY = buffer.readShort();
            } else if (opcode == 72) {
                translateZ = buffer.readShort();
            } else if (opcode == 73) {
                obstructsGround = true;
            } else if (opcode == 74) {
                hollow = true;
            } else if (opcode == 75) {
                supportItems = buffer.readUByte();
            } else if (opcode == 77) {
                varbit = buffer.readUShort();
                if (varbit == 65535) {
                    varbit = -1;
                }

                varp = buffer.readUShort();
                if (varp == 65535) {
                    varp = -1;
                }

                int count = buffer.readUByte();
                morphisms = new int[count + 1];
                for (int i = 0; i <= count; i++) {
                    morphisms[i] = buffer.readUShort();
                    if (morphisms[i] == 65535) {
                        morphisms[i] = -1;
                    }
                }
            } else {
                System.out.println("Unrecognised object opcode " + opcode);
            }
        }

        if (interactive == -1) {
            this.interactive = ((modelIds != null && (modelTypes == null || modelTypes[0] == 10)) || interactions != null);
        }

        if (hollow) {
            solid = false;
            impenetrable = false;
        }

        if (supportItems == -1) {
            supportItems = solid ? 1 : 0;
        }
    }

    @Override
    protected void encode(RSBuffer buffer) {
        if (modelIds != null) {
            if (modelTypes != null) {
                buffer.writeByte(1);
                buffer.writeByte(modelIds.length);

                if (modelIds.length > 0) {
                    for (int i = 0; i < modelIds.length; i++) {
                        buffer.writeShort(modelIds[i]);
                        buffer.writeByte(modelTypes[i]);
                    }
                }
            } else {
                buffer.writeByte(5);
                buffer.writeByte(modelIds.length);
                if (modelIds.length > 0) {
                    for (int i = 0; i < modelIds.length; i++) {
                        buffer.writeShort(modelIds[i]);
                    }
                }
            }
        }

        if (name != null) {
            buffer.writeByte(2);
            buffer.writeString10(name);
        }

        if (description != null) {
            buffer.writeByte(3);
            buffer.writeString10(description);
        }

        if (width != 1) {
            buffer.writeByte(14);
            buffer.writeByte(width);
        }

        if (length != 1) {
            buffer.writeByte(15);
            buffer.writeByte(length);
        }

        if (!solid) {
            buffer.writeByte(17);
        }

        if (!impenetrable) {
            buffer.writeByte(18);
        }

        if (interactive) {
            buffer.writeByte(19);
            buffer.writeByte(1);
        }

        if (contouredGround) {
            buffer.writeByte(21);
        }

        if (delayShading) {
            buffer.writeByte(22);
        }

        if (occludes) {
            buffer.writeByte(23);
        }

        if (animation != -1) {
            buffer.writeByte(24);
            buffer.writeShort(animation);
        }

        if (decorDisplacement != 16) {
            buffer.writeByte(28);
            buffer.writeByte(decorDisplacement);
        }

        if (ambientLighting != 0) {
            buffer.writeByte(29);
            buffer.writeByte(ambientLighting);
        }

        if (lightDiffusion != 0) {
            buffer.writeByte(39);
            buffer.writeByte(lightDiffusion);
        }

        if (interactions != null) {
            for (int i = 0; i < interactions.length; i++) {
                if (interactions[i] == null) {
                    continue;
                }
                buffer.writeByte(30 + i);
                buffer.writeString10(interactions[i]);
            }
        }

        if (originalColours != null && replacementColours != null) {
            buffer.writeByte(40);
            buffer.writeByte(originalColours.length);
            for (int i = 0; i < originalColours.length; i++) {
                buffer.writeShort(originalColours[i]);
                buffer.writeShort(replacementColours[i]);
            }
        }

        if (minimapFunction != -1) {
            buffer.writeByte(60);
            buffer.writeShort(minimapFunction);
        }

        if (inverted) {
            buffer.writeByte(62);
        }

        if (!castsShadow) {
            buffer.writeByte(64);
        }

        if (scaleX != 128) {
            buffer.writeByte(65);
            buffer.writeShort(scaleX);
        }

        if (scaleY != 128) {
            buffer.writeByte(66);
            buffer.writeShort(scaleY);
        }

        if (scaleZ != 128) {
            buffer.writeByte(67);
            buffer.writeShort(scaleZ);
        }

        if (mapscene != -1) {
            buffer.writeByte(68);
            buffer.writeShort(mapscene);
        }

        if (surroundings != 0) {
            buffer.writeByte(69);
            buffer.writeByte(surroundings);
        }

        if (translateX != 0) {
            buffer.writeByte(70);
            buffer.writeShort(translateX);
        }

        if (translateY != 0) {
            buffer.writeByte(71);
            buffer.writeShort(translateY);
        }

        if (translateZ != 0) {
            buffer.writeByte(72);
            buffer.writeShort(translateZ);
        }

        if (obstructsGround) {
            buffer.writeByte(73);
        }

        if (hollow) {
            buffer.writeByte(74);
        }

        if (supportItems != -1) {
            buffer.writeByte(75);
            buffer.writeByte(supportItems);
        }

        if ((varbit != -1 || varp != -1) && morphisms != null) {
            buffer.writeByte(77);
            buffer.writeShort(varbit);
            buffer.writeShort(varp);

            buffer.writeByte(morphisms.length - 1);

            for (int i = 0; i <= morphisms.length - 1; i++) {
                buffer.writeShort(morphisms[i]);
            }
        }

        buffer.writeByte(0);
    }

    private byte ambientLighting;
    private int animation = -1;
    private boolean castsShadow = true;
    private boolean contouredGround;
    private int decorDisplacement = 16;
    private boolean delayShading;
    private String description;
    private boolean hollow;
    private int id = -1;
    private boolean impenetrable = true;
    private String[] interactions;
    private boolean interactive;
    private boolean inverted;
    private int length = 1;
    private byte lightDiffusion;
    private int mapscene = -1;
    private int minimapFunction = -1;
    private int[] modelIds;
    private int[] modelTypes;
    private int[] morphisms;
    private int varbit = -1;
    private int varp = -1;
    private String name;
    private boolean obstructsGround;
    private boolean occludes;
    private int[] originalColours;
    private int[] replacementColours;
    private int scaleX = 128;
    private int scaleY = 128;
    private int scaleZ = 128;
    private boolean solid = true;
    private int supportItems = -1;
    private int surroundings;
    private int translateX;
    private int translateY;
    private int translateZ;
    private int width = 1;

}
