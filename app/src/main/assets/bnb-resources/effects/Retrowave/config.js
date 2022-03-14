
var settings = {
	effectName: "Retrowave"
};

function timeUpdate() {
    if (effect.lastTime === undefined) effect.lastTime = (new Date()).getTime();

    var now = (new Date()).getTime();
    effect.delta = now - effect.lastTime;
    if (effect.delta < 3000) { // dont count spend time if application is minimized
        spendTime += effect.delta;
    }
    effect.lastTime = now;
}

function Effect() {
    var self = this;
	var c = 0.5;
	var sec = 0;
	var lastFrame;
	this.play = function() {
		var now = (new Date()).getTime();
		sec += (now - lastFrame)/1000;
		Api.meshfxMsg("shaderVec4", 0, 0, String(sec));
		//Api.showHint(String(sec));
		lastFrame = now;
    }
    
    this.init = function() {
        Api.meshfxMsg("spawn", 2, 0, "!glfx_FACE");
        Api.meshfxMsg("spawn", 3, 0, "gl_01.bsm2");
        Api.meshfxMsg("spawn", 1, 0, "BeautyFace.bsm2");
        Api.meshfxMsg("spawn", 0, 0, "tri.bsm2");
        lastFrame = (new Date()).getTime();

        Api.playVideo("frx",true,1);
        // Api.playSound("sfx.aac",false,1);
    };

    this.restart = function() {
        Api.meshfxReset();
        // Api.stopVideo("frx");
        // Api.stopSound("sfx.aac");
        self.init();
    };

	this.faceActions = [self.play, timeUpdate];
    this.noFaceActions = [timeUpdate];

    this.videoRecordStartActions = [];
    this.videoRecordFinishActions = [];
    this.videoRecordDiscardActions = [this.restart];
}

configure(new Effect());