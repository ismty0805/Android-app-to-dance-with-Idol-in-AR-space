using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Recorder;

public class UIManager : MonoBehaviour
{
    public RecordManager recordManager;
    public void StartVid(){
        recordManager.StartRecord();
    }
    public void StopVid(){
        recordManager.StopRecord();
    }
}