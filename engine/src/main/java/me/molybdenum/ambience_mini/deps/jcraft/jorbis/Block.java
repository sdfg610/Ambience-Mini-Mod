/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *  
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *   
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package me.molybdenum.ambience_mini.deps.jcraft.jorbis;

import me.molybdenum.ambience_mini.deps.jcraft.jogg.Buffer;
import me.molybdenum.ambience_mini.deps.jcraft.jogg.Packet;

import java.io.IOException;

public class Block
{
  ///necessary stream state for linking to the framing abstraction
  float[][] pcm=new float[0][]; // this is a pointer into local storage
  final Buffer opb=new Buffer();

  int lW;
  int W;
  int nW;
  int pcmend;
  int mode;

  int eofflag;
  long granulepos;
  long sequence;
  DspState vd; // For read-only access of configuration


  //
  // Save state
  //
  float[][] saved_pcm; // this is a pointer into local storage

  int saved_lW;
  int saved_W;
  int saved_nW;
  int saved_pcmend;
  int saved_mode;

  int saved_eofflag;
  long saved_granulepos;
  long saved_sequence;


  public Block(DspState vd){
    this.vd=vd;
    if(vd.analysisp!=0){
      opb.writeinit();
    }
  }


  public void saveState() {
    int len = pcm.length;
    saved_pcm = new float[len][];
    for (int i = 0; i < len; i++) {
      float[] arr = pcm[i];
      saved_pcm[i] = new float[arr.length];
      System.arraycopy(arr, 0, saved_pcm[i], 0, arr.length);
    }
    opb.saveState();

    saved_lW = lW;
    saved_W = W;
    saved_nW = nW;
    saved_pcmend = pcmend;
    saved_mode = mode;
    saved_eofflag = eofflag;
    saved_granulepos = granulepos;
    saved_sequence = sequence;
  }

  public void restoreState() {
    int len = saved_pcm.length;
    pcm = new float[len][];
    for (int i = 0; i < len; i++) {
      float[] arr = saved_pcm[i];
      pcm[i] = new float[arr.length];
      System.arraycopy(arr, 0, pcm[i], 0, arr.length);
    }
    opb.restoreState();

    lW = saved_lW;
    W = saved_W;
    nW = saved_nW;
    pcmend = saved_pcmend;
    mode = saved_mode;
    eofflag = saved_eofflag;
    granulepos = saved_granulepos;
    sequence = saved_sequence;
  }


  public void init(DspState vd){
    this.vd=vd;
  }

  public int clear(){
    if(vd!=null){
      if(vd.analysisp!=0){
        opb.writeclear();
      }
    }
    return (0);
  }

  public int synthesis(Packet op){
    Info vi=vd.vi;

    // first things first.  Make sure decode is ready
    opb.readinit(op.packet_base, op.packet, op.bytes);

    // Check the packet type
    if(opb.read(1)!=0){
      // Oops.  This is not an audio data packet
      return (-1);
    }

    // read our mode and pre/post windowsize
    int _mode=opb.read(vd.modebits);
    if(_mode==-1)
      return (-1);

    mode=_mode;
    W=vi.mode_param[mode].blockflag;
    if(W!=0){
      lW=opb.read(1);
      nW=opb.read(1);
      if(nW==-1)
        return (-1);
    }
    else{
      lW=0;
      nW=0;
    }

    // more setup
    granulepos=op.granulepos;
    sequence=op.packetno-3; // first block is third packet
    eofflag=op.e_o_s;

    // alloc pcm passback storage
    pcmend=vi.blocksizes[W];
    if(pcm.length<vi.channels){
      pcm=new float[vi.channels][];
    }
    for(int i=0; i<vi.channels; i++){
      if(pcm[i]==null||pcm[i].length<pcmend){
        pcm[i]=new float[pcmend];
      }
      else{
        for(int j=0; j<pcmend; j++){
          pcm[i][j]=0;
        }
      }
    }

    // unpack_header enforces range checking
    int type=vi.map_type[vi.mode_param[mode].mapping];
    return (FuncMapping.mapping_P[type].inverse(this, vd.mode[mode]));
  }
}
